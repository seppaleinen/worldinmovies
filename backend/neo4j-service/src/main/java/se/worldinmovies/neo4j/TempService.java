package se.worldinmovies.neo4j;

import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Flux;
import se.worldinmovies.neo4j.domain.Genre;
import se.worldinmovies.neo4j.entity.GenreEntity;
import se.worldinmovies.neo4j.repository.CountryRepository;
import se.worldinmovies.neo4j.repository.GenreRepository;
import se.worldinmovies.neo4j.repository.LanguageRepository;
import se.worldinmovies.neo4j.repository.MovieRepository;

@Service
public class TempService {
    private final ReactiveNeo4jTemplate template;
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final LanguageRepository languageRepository;
    private final CountryRepository countryRepository;
    private final TmdbService tmdbService;
    private final ReactiveTransactionManager txManager;

    public TempService(ReactiveNeo4jTemplate template, MovieRepository movieRepository, GenreRepository genreRepository, LanguageRepository languageRepository, CountryRepository countryRepository, TmdbService tmdbService, ReactiveTransactionManager txManager) {
        this.template = template;
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
        this.languageRepository = languageRepository;
        this.countryRepository = countryRepository;
        this.tmdbService = tmdbService;
        this.txManager = txManager;
    }

    public Flux<GenreEntity> handleGenres() {
        return tmdbService.getData("/dump/genres", Genre.class)
                .map(a -> new GenreEntity(a.getId(), a.getName()))
                .buffer(10)
                .log("Persisting")
                .flatMap(template::saveAll);
    }

}
