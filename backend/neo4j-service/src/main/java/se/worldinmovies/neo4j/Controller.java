package se.worldinmovies.neo4j;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.worldinmovies.neo4j.entity.MovieEntity;
import se.worldinmovies.neo4j.xml.LanguageMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
public class Controller {
    private final TmdbService tmdbService;
    private final LanguageMapper languageMapper;
    private final ReactiveNeo4jTemplate neo4jTemplate;

    public Controller(TmdbService tmdbService, LanguageMapper languageMapper, ReactiveNeo4jTemplate neo4jTemplate) {
        this.tmdbService = tmdbService;
        this.languageMapper = languageMapper;
        this.neo4jTemplate = neo4jTemplate;
    }

    @GetMapping(value = "/status")
    Mono<Status> status() {
        Mono<Status> last = tmdbService.getData("/status", Status.class).last();
        return last.zipWith(neo4jTemplate.count(MovieEntity.class), (tmdbServiceResult, count) ->
                Status.createFromEntity(count, tmdbServiceResult));
    }

    @GetMapping(value = "/view/best/{countryCode}")
    Flux<JsonMovie> findBestFromCountry(@PathVariable String countryCode) {
        List<String> languagesFromCountryCode = languageMapper.getLanguagesFromCountryCode(countryCode);
        List<String> newCountryCode = CountryMapper.getOldFromNew(countryCode);

        String query = "MATCH (l:Language)-[lr]->(m:Movie)-[cr:produced_by]->(c:Country) " +
                "WHERE c.iso in ($countryCode) " +
                "AND l.iso in ($languageCodes) " +
                "RETURN DISTINCT m " +
                "ORDER BY m.weight DESC " +
                "SKIP $skip LIMIT $limit";

        Map<String, Object> params = Map.of(
                "countryCode", newCountryCode,
                "languageCodes", languagesFromCountryCode,
                "skip", 0,
                "limit", 25);

        return neo4jTemplate.findAll(query, params, MovieEntity.class)
                .map(JsonMovie::createFromEntity);
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
}

