package se.worldinmovies.neo4j;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import se.worldinmovies.neo4j.domain.Movie;
import se.worldinmovies.neo4j.domain.Votes;
import se.worldinmovies.neo4j.entity.CountryEntity;
import se.worldinmovies.neo4j.entity.GenreEntity;
import se.worldinmovies.neo4j.entity.LanguageEntity;
import se.worldinmovies.neo4j.entity.MovieEntity;
import se.worldinmovies.neo4j.repository.MovieRepository;
import se.worldinmovies.neo4j.xml.LanguageMapper;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class Neo4jService {
    private final MovieRepository movieRepository;
    private final TmdbService tmdbService;
    private final ImdbService imdbService;
    private final ReactiveNeo4jTemplate neo4jTemplate;

    public Neo4jService(MovieRepository movieRepository,
                        TmdbService tmdbService,
                        ReactiveNeo4jTemplate neo4jTemplate,
                        ImdbService imdbService) {
        this.neo4jTemplate = neo4jTemplate;
        this.movieRepository = movieRepository;
        this.tmdbService = tmdbService;
        this.imdbService = imdbService;
    }

    public Mono<Map<Integer, GenreEntity>> getGenres() {
        return neo4jTemplate.findAll(GenreEntity.class)
                .collectMap(GenreEntity::getId)
                .cache();
    }

    public Mono<Map<String, LanguageEntity>> getLanguages() {
        return neo4jTemplate.findAll(LanguageEntity.class)
                .collectMap(LanguageEntity::getIso)
                .cache();
    }

    public Mono<Map<String, CountryEntity>> getCountries() {
        return neo4jTemplate.findAll(CountryEntity.class)
                .collectMap(CountryEntity::getIso)
                .cache();
    }

    private Mono<List<Votes>> getImdbVotes(String movieIds) {
        return imdbService.getData("/votes/" + movieIds, Votes.class)
                .onErrorComplete(WebClientResponseException.NotFound.class)
                .collectList()
                .cache();
    }

    private Mono<Map<Integer, MovieEntity>> getMovies(Collection<Integer> value) {
        return movieRepository.findAllById(value)
                .collectMap(MovieEntity::getMovieId)
                .cache();
    }

    public Flux<Integer> handleNewAndUpdates(Collection<Integer> movieIds) {
        Mono<Map<Integer, MovieEntity>> existingMovies = getMovies(movieIds);

        String movieIdsAsString = movieIds.stream().map(Object::toString).collect(Collectors.joining(","));
        Flux<Movie> data = tmdbService.getData("/movie/" + movieIdsAsString, Movie.class);
        Mono<List<Votes>> votes = getImdbVotes(movieIdsAsString);

        return movieRepository.saveAll(data
                        .flatMap(fetchedMovie ->
                                Mono.zip(Mono.just(fetchedMovie), existingMovies, getGenres(), getLanguages(), getCountries(), votes)
                                        .map(a -> {
                                            Movie movie = a.getT1();
                                            return createNewOrUpdate(movie, a.getT2())
                                                    .withData(movie)
                                                    .withGenres(a.getT3())
                                                    .withLanguages(a.getT4())
                                                    .withCountries(a.getT5())
                                                    .withVotes(a.getT6());
                                        }), 5))
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(300))
                        .filter(a -> a instanceof TransientDataAccessException))
                .map(MovieEntity::getMovieId);
    }

    private static MovieEntity createNewOrUpdate(Movie movie, Map<Integer, MovieEntity> map) {
        return map.getOrDefault(movie.getMovieId(), new MovieEntity(movie.getMovieId())).withData(movie);
    }

    public Mono<Void> delete(Collection<Integer> value) {
        return movieRepository.deleteAllById(value);
    }
}
