package se.worldinmovies.neo4j;

import lombok.extern.java.Log;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import se.worldinmovies.neo4j.domain.Country;
import se.worldinmovies.neo4j.domain.Genre;
import se.worldinmovies.neo4j.domain.Language;
import se.worldinmovies.neo4j.domain.Movie;
import se.worldinmovies.neo4j.entity.CountryEntity;
import se.worldinmovies.neo4j.entity.GenreEntity;
import se.worldinmovies.neo4j.entity.LanguageEntity;
import se.worldinmovies.neo4j.entity.MovieEntity;
import se.worldinmovies.neo4j.repository.CountryRepository;
import se.worldinmovies.neo4j.repository.GenreRepository;
import se.worldinmovies.neo4j.repository.LanguageRepository;
import se.worldinmovies.neo4j.repository.MovieRepository;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Log
public class Neo4jService {
    private final MovieRepository movieRepository;
    private final TmdbService tmdbService;
    private final ReactiveNeo4jTemplate neo4jTemplate;

    private Map<String, CountryEntity> countries;
    private Map<String, LanguageEntity> languages;
    private Map<Integer, GenreEntity> genres;

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
            genres = handle("/dump/genres", Genre.class, a -> new GenreEntity(a.getId(), a.getName()))
                    .collectMap(GenreEntity::getId)
                    .blockOptional(Duration.ofSeconds(5)).orElse(Map.of());

            languages = handle("/dump/langs", Language.class, a -> new LanguageEntity(a.getIso(), a.getName(), a.getEnglishName()))
                    .collectMap(LanguageEntity::getIso)
                    .blockOptional(Duration.ofSeconds(5)).orElse(Map.of());

            countries = handle("/dump/countries", Country.class, a -> new CountryEntity(a.getIso(), a.getName(), List.of()))
                    .collectMap(CountryEntity::getIso)
                    .blockOptional(Duration.ofSeconds(5)).orElse(Map.of());
        } catch (Exception e) {
            log.info("Something went wrong: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public <T, E> Flux<E> handle(String url, Class<T> domain, Function<T, E> domainToEntityMapper) {
        return tmdbService.getData(url, domain)
                .map(domainToEntityMapper)
                .buffer(10)
                .log("Persisting")
                .flatMap(neo4jTemplate::saveAll)
                .publish(20);
    }

    public Flux<MovieEntity> handleNewAndUpdates(Collection<Integer> value) {
        String movieIds = value.stream().map(Object::toString).collect(Collectors.joining(","));

        return movieRepository.saveAll(tmdbService.getData("/movie/" + movieIds, Movie.class)
                        .flatMap(movie -> movieRepository.findById(movie.getMovieId())
                                .map(movieEntity -> movieEntity.updateWith(movie, genres, languages, countries))
                                .switchIfEmpty(Mono.just(new MovieEntity(movie, genres, languages, countries)))
                                .flux()
                        )
                        .onBackpressureBuffer()
                )
                .publishOn(Schedulers.immediate())
                .publish();
    }

    public Mono<Void> delete(Collection<Integer> value) {
        return movieRepository.deleteAllById(value);
    }
}
