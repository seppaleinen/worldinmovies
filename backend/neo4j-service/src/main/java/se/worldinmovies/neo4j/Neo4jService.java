package se.worldinmovies.neo4j;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import se.worldinmovies.neo4j.domain.Country;
import se.worldinmovies.neo4j.domain.Genre;
import se.worldinmovies.neo4j.domain.Language;
import se.worldinmovies.neo4j.domain.Movie;
import se.worldinmovies.neo4j.entity.CountryEntity;
import se.worldinmovies.neo4j.entity.GenreEntity;
import se.worldinmovies.neo4j.entity.LanguageEntity;
import se.worldinmovies.neo4j.entity.MovieEntity;
import se.worldinmovies.neo4j.repository.MovieRepository;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class Neo4jService {
    private static final Map<Integer, GenreEntity> genres = new ConcurrentHashMap<>();
    private static final Map<String, LanguageEntity> languages = new ConcurrentHashMap<>();
    private static final Map<String, CountryEntity> countries = new ConcurrentHashMap<>();

    private static final CountDownLatch genreLatch = new CountDownLatch(1);
    private static final CountDownLatch langLatch = new CountDownLatch(1);
    private static final CountDownLatch countryLatch = new CountDownLatch(1);

    private final MovieRepository movieRepository;
    private final TmdbService tmdbService;


    public Neo4jService(MovieRepository movieRepository,
                        TmdbService tmdbService,
                        ReactiveNeo4jTemplate neo4jTemplate) {
        this.movieRepository = movieRepository;
        this.tmdbService = tmdbService;
        handle(neo4jTemplate, tmdbService, "/dump/genres", Genre.class, GenreEntity.class, a -> new GenreEntity(a.getId(), a.getName()), Genre::getId, GenreEntity::getId)
                .collectMap(GenreEntity::getId)
                .subscribe(genres::putAll, err -> {
                }, genreLatch::countDown);
        handle(neo4jTemplate, tmdbService, "/dump/langs", Language.class, LanguageEntity.class, a -> new LanguageEntity(a.getIso(), a.getName(), a.getEnglishName()), Language::getIso, LanguageEntity::getIso)
                .collectMap(LanguageEntity::getIso)
                .subscribe(languages::putAll, err -> {
                }, langLatch::countDown);
        handle(neo4jTemplate, tmdbService, "/dump/countries", Country.class, CountryEntity.class, a -> new CountryEntity(a.getIso(), a.getName(), List.of()), Country::getIso, CountryEntity::getIso)
                .collectMap(CountryEntity::getIso)
                .subscribe(countries::putAll, err -> {
                }, countryLatch::countDown);
    }


    public Mono<Map<Integer, GenreEntity>> getGenres() {
        return getCached(genreLatch, genres);
    }

    public Mono<Map<String, LanguageEntity>> getLanguages() {
        return getCached(langLatch, languages);
    }

    public Mono<Map<String, CountryEntity>> getCountries() {
        return getCached(countryLatch, countries);
    }

    @SneakyThrows
    public <ID, E> Mono<Map<ID, E>> getCached(CountDownLatch countDownLatch, Map<ID, E> map) {
        if (countDownLatch.getCount() != 0) {
            countDownLatch.await();
        }
        return Mono.just(map);

    }

    @Transactional
    public <D, E, ID> Flux<E> handle(ReactiveNeo4jTemplate neo4jTemplate,
                                     TmdbService tmdbService,
                                     String url,
                                     Class<D> domain,
                                     Class<E> entity,
                                     Function<D, E> domainToEntityMapper,
                                     Function<D, ID> getId,
                                     Function<E, ID> getDomainId) {
        Mono<List<E>> idsFlux = neo4jTemplate.findAll(entity)
                .collectList();
        Mono<List<D>> data = tmdbService.getData(url, domain)
                .collectList();
        return Mono.zip(data, idsFlux, (movies, fetched) -> movies.stream()
                        .filter(movie -> fetched.stream().map(getDomainId).noneMatch(id -> id == getId.apply(movie)))
                        .collect(Collectors.toList()))
                .flatMapMany(Flux::fromIterable)
                .map(domainToEntityMapper)
                .onBackpressureBuffer()
                .bufferTimeout(25, Duration.ofMillis(100))
                .map(a -> neo4jTemplate.saveAllAs(a, entity))
                .mergeWith(idsFlux.flux().map(a -> Flux.fromStream(a.stream())))
                .flatMap(a -> a);
    }

    public Flux<Integer> handleNewAndUpdates(Collection<Integer> value) {
        String movieIds = value.stream().map(Object::toString).collect(Collectors.joining(","));
        Flux<Movie> data = tmdbService.getData("/movie/" + movieIds, Movie.class);
        Mono<Map<Integer, MovieEntity>> existingMovies = movieRepository.findAllById(value)
                .collectMap(MovieEntity::getMovieId)
                .cache();

        return movieRepository.saveAll(data
                        .flatMap(fetchedMovie -> Mono.just(fetchedMovie)
                                .zipWith(existingMovies, Neo4jService::createNewOrUpdate)
                                .zipWith(getGenres(), MovieEntity::withGenres)
                                .zipWith(getLanguages(), MovieEntity::withLanguages)
                                .zipWith(getCountries(), MovieEntity::withCountries), 5))
                .retryWhen(Retry.fixedDelay(5, Duration.ofMillis(300)))
                .map(MovieEntity::getMovieId);
    }

    private static MovieEntity createNewOrUpdate(Movie movie, Map<Integer, MovieEntity> map) {
        return map.getOrDefault(movie.getMovieId(), new MovieEntity(movie.getMovieId())).withData(movie);
    }

    public Mono<Void> delete(Collection<Integer> value) {
        return movieRepository.deleteAllById(value);
    }
}
