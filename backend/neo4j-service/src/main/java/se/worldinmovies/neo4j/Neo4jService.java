package se.worldinmovies.neo4j;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple3;
import se.worldinmovies.neo4j.domain.Country;
import se.worldinmovies.neo4j.domain.Genre;
import se.worldinmovies.neo4j.domain.Language;
import se.worldinmovies.neo4j.domain.Movie;
import se.worldinmovies.neo4j.entity.CountryEntity;
import se.worldinmovies.neo4j.entity.GenreEntity;
import se.worldinmovies.neo4j.entity.LanguageEntity;
import se.worldinmovies.neo4j.entity.MovieEntity;
import se.worldinmovies.neo4j.repository.MovieRepository;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class Neo4jService {
    private final MovieRepository movieRepository;
    private final TmdbService tmdbService;
    private final ReactiveNeo4jTemplate neo4jTemplate;

    private static final Map<Integer, GenreEntity> genres = new HashMap<>();
    private static final Map<String, LanguageEntity> languages = new HashMap<>();
    private static final Map<String, CountryEntity> countries = new HashMap<>();

    public Neo4jService(MovieRepository movieRepository,
                        TmdbService tmdbService,
                        ReactiveNeo4jTemplate neo4jTemplate) {
        this.movieRepository = movieRepository;
        this.tmdbService = tmdbService;
        this.neo4jTemplate = neo4jTemplate;
    }

    @PostConstruct
    public void setup() {
        try {
            getGenres().subscribe(genres::putAll);
            getLanguages().subscribe(languages::putAll);
            getCountries().subscribe(countries::putAll);
        } catch (Exception e) {
            log.info("Something went wrong: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Mono<Map<Integer, GenreEntity>> getGenres() {
        if (genres.isEmpty()) {
            return handle("/dump/genres", Genre.class, GenreEntity.class, a -> new GenreEntity(a.getId(), a.getName()), Genre::getId, GenreEntity::getId)
                    .collectMap(GenreEntity::getId);
        } else {
            return Mono.just(genres);
        }
    }

    public Mono<Map<String, LanguageEntity>> getLanguages() {
        if (languages.isEmpty()) {
            return handle("/dump/langs", Language.class, LanguageEntity.class, a -> new LanguageEntity(a.getIso(), a.getName(), a.getEnglishName()), Language::getIso, LanguageEntity::getIso)
                    .collectMap(LanguageEntity::getIso);
        } else {
            return Mono.just(languages);
        }
    }

    public Mono<Map<String, CountryEntity>> getCountries() {
        if (countries.isEmpty()) {
            return handle("/dump/countries", Country.class, CountryEntity.class, a -> new CountryEntity(a.getIso(), a.getName(), List.of()), Country::getIso, CountryEntity::getIso)
                    .collectMap(CountryEntity::getIso);
        } else {
            return Mono.just(countries);
        }
    }

    public <D, E, ID> Flux<E> handle(String url,
                                     Class<D> domain,
                                     Class<E> entity,
                                     Function<D, E> domainToEntityMapper,
                                     Function<D, ID> getId,
                                     Function<E, ID> getDomainId) {
        Mono<List<ID>> idsFlux = neo4jTemplate.findAll(entity)
                .map(getDomainId)
                .collectList();
        Mono<List<D>> data = tmdbService.getData(url, domain)
                .collectList();
        return Flux.zip(data, idsFlux, (movies, ids) -> Flux.fromStream(movies.stream()
                        .filter(d -> !ids.contains(getId.apply(d)))))
                .flatMap(a -> a)
                .map(domainToEntityMapper)
                .bufferTimeout(10, Duration.ofMillis(10))
                .flatMap(neo4jTemplate::saveAll, 5)
                .thenMany(neo4jTemplate.findAll(entity));
    }

    public Flux<MovieEntity> handleNewAndUpdates(Collection<Integer> value) {
        Mono<Map<Integer, GenreEntity>> genresFlux = getGenres();
        Mono<Map<String, LanguageEntity>> langFlux = getLanguages();
        Mono<Map<String, CountryEntity>> countryFlux = getCountries();
        String movieIds = value.stream().map(Object::toString).collect(Collectors.joining(","));
        Flux<Movie> data = tmdbService.getData("/movie/" + movieIds, Movie.class);
        return movieRepository.saveAll(data.map(MovieEntity::new)
                        .zipWith(genresFlux, MovieEntity::withGenres)
                        .zipWith(langFlux, MovieEntity::withLanguages)
                        .zipWith(countryFlux, MovieEntity::withCountries)
                //.filterWhen(data -> movieRepository.existsById(data.getMovieId()).map(result -> !result))
        );
    }

    public Mono<Void> delete(Collection<Integer> value) {
        return movieRepository.deleteAllById(value);
    }
}
