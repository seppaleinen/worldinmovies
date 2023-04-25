package se.worldinmovies.neo4j;

import jakarta.websocket.server.PathParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.worldinmovies.neo4j.domain.Movie;
import se.worldinmovies.neo4j.entity.MovieEntity;
import se.worldinmovies.neo4j.repository.MovieRepository;
import se.worldinmovies.neo4j.xml.LanguageMapper;

import java.util.List;

@RestController
@Slf4j
public class Controller {
    private final MovieRepository movieRepository;
    private final TmdbService tmdbService;
    private final LanguageMapper languageMapper;

    public Controller(MovieRepository movieRepository, TmdbService tmdbService, LanguageMapper languageMapper) {
        this.movieRepository = movieRepository;
        this.tmdbService = tmdbService;
        this.languageMapper = languageMapper;
    }

    @GetMapping(value = "/status")
    Mono<Status> status() {
        Mono<Status> last = tmdbService.getData("/status", Status.class).last();
        return last.zipWith(movieRepository.count(), (tmdbServiceResult, count) ->
                Status.createFromEntity(count, tmdbServiceResult));
    }

    @GetMapping(value = "/view/best/{countryCode}")
    Flux<JsonMovie> findBestFromCountry(@PathVariable String countryCode,
                                        @RequestParam(value = "by", required = false, defaultValue = "PRODUCED_BY") By by) {
        final List<String> languagesFromCountryCode = languageMapper.getLanguagesFromCountryCode(countryCode);
        Flux<MovieEntity> a;
        switch (by) {
            case PRODUCED_BY -> a = movieRepository.findBestByProducerCountry(countryCode, languagesFromCountryCode,
                            0, 25)
                    .log("produced");
            case LANGUAGE -> a = movieRepository.findBestByLanguage(languagesFromCountryCode, 0, 25)
                    .log("lang");
            default -> a = Flux.empty();
        }
        return a.map(JsonMovie::createFromEntity)
                .distinct(JsonMovie::id);
    }

    public record Status(long total, long fetched, double percentageDone) {
        static Status createFromEntity(long count, Status tmdbStatus) {
            double percentage = ((double) count / (double) tmdbStatus.total) * 100;
            return new Status(tmdbStatus.fetched, count, percentage);
        }
    }

    public record JsonMovie(int id, String title, String engTitle, String posterPath) {
        static JsonMovie createFromEntity(MovieEntity movie) {
            return new JsonMovie(movie.getMovieId(),
                    movie.getOriginalTitle(),
                    movie.getEngTitle(),
                    movie.getPosterPath());
        }
    }

    public enum By {
        PRODUCED_BY,
        LANGUAGE
    }
}

