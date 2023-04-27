package se.worldinmovies.neo4j;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.worldinmovies.neo4j.entity.MovieEntity;
import se.worldinmovies.neo4j.repository.MovieRepository;
import se.worldinmovies.neo4j.xml.LanguageMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
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
        List<String> languagesFromCountryCode = languageMapper.getLanguagesFromCountryCode(countryCode);
        String newCountryCode = CountryMapper.getOldFromNew(countryCode);
        Flux<MovieEntity> a;
        switch (by) {
            case PRODUCED_BY -> a = movieRepository.findBestByProducerCountry(newCountryCode, languagesFromCountryCode,
                            0, 25);
            case LANGUAGE -> a = movieRepository.findBestByLanguage(languagesFromCountryCode, 0, 25);
            default -> a = Flux.empty();
        }
        return a.map(JsonMovie::createFromEntity)
                .distinct(JsonMovie::id);
    }

    record Status(long total, long fetched, @JsonProperty("percentageDone") BigDecimal percentageDone) {
        static Status createFromEntity(long count, Status tmdbStatus) {
            double percentage = ((double) count / (double) tmdbStatus.total) * 100;
            return new Status(tmdbStatus.fetched, count, new BigDecimal(String.valueOf(percentage)).setScale(2, RoundingMode.HALF_UP));
        }
    }

    record JsonMovie(String imdbId,
                            int id,
                            String originalTitle,
                            String enTitle,
                            String posterPath,
                            String releaseDate,
                            BigDecimal voteAverage,
                            int voteCount) {
        static JsonMovie createFromEntity(MovieEntity movie) {
            return new JsonMovie(
                    movie.getImdbId(),
                    movie.getMovieId(),
                    movie.getOriginalTitle(),
                    movie.getEngTitle(),
                    movie.getPosterPath(),
                    movie.getReleaseDate(),
                    new BigDecimal(String.valueOf(movie.getVoteAverage())).setScale(1, RoundingMode.HALF_UP),
                    movie.getVoteCount());
        }
    }

    enum By {
        PRODUCED_BY,
        LANGUAGE
    }
}

