package se.worldinmovies.neo4j;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class Neo4jService {
    private final MovieRepository movieRepository;
    private final TmdbService tmdbService;
    private final ReactiveNeo4jTemplate neo4jTemplate;

    private Map<String, CountryEntity> countries = new HashMap<>();
    private Map<String, LanguageEntity> languages = new HashMap<>();
    private Map<Integer, GenreEntity> genres = new HashMap<>();

    private final AtomicBoolean initDone = new AtomicBoolean(false);

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
            genres = handle("/dump/genres", Genre.class, GenreEntity.class, a -> new GenreEntity(a.getId(), a.getName()), Genre::getId)
                    .collectMap(GenreEntity::getId)
                    .blockOptional(Duration.ofSeconds(5)).orElse(Map.of());

            languages = handle("/dump/langs", Language.class, LanguageEntity.class, a -> new LanguageEntity(a.getIso(), a.getName(), a.getEnglishName()), Language::getIso)
                    .collectMap(LanguageEntity::getIso)
                    .blockOptional(Duration.ofSeconds(5)).orElse(Map.of());

            countries = handle("/dump/countries", Country.class, CountryEntity.class, a -> new CountryEntity(a.getIso(), a.getName(), List.of()), Country::getIso)
                    .collectMap(CountryEntity::getIso)
                    .blockOptional(Duration.ofSeconds(5)).orElse(Map.of());

            initDone.set(true);
        } catch (Exception e) {
            log.info("Something went wrong: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public <T, E> Flux<E> handle(String url,
                                 Class<T> domain,
                                 Class<E> entity,
                                 Function<T, E> domainToEntityMapper,
                                 Function<T, ?> getId) {
        return tmdbService.getData(url, domain)
                .filterWhen(a -> neo4jTemplate.existsById(getId.apply(a), entity).map(result -> !result))
                .flatMap(a -> Flux.just(domainToEntityMapper.apply(a)))
                .bufferTimeout(10, Duration.ofMillis(10))
                .flatMap(neo4jTemplate::saveAll)
                .thenMany(neo4jTemplate.findAll(entity));
    }

    public Flux<MovieEntity> handleNewAndUpdates(Collection<Integer> value) {
        String movieIds = value.stream().map(Object::toString).collect(Collectors.joining(","));
        return movieRepository.saveAll(tmdbService.getData("/movie/" + movieIds, Movie.class)
                //.filterWhen(data -> movieRepository.existsById(data.getMovieId()).map(result -> !result))
                .map(a -> new MovieEntity(a, genres, languages, countries))
        );
    }

    public Mono<Void> delete(Collection<Integer> value) {
        return movieRepository.deleteAllById(value);
    }

    public boolean isDone() {
        return initDone.get();
    }
}
