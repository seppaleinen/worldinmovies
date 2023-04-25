package se.worldinmovies.neo4j;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.worldinmovies.neo4j.domain.Movie;
import se.worldinmovies.neo4j.repository.MovieRepository;

@RestController
@Slf4j
public class Controller {
	private final MovieRepository movieRepository;
	private final TmdbService tmdbService;

	public Controller(MovieRepository movieRepository, TmdbService tmdbService) {
		this.movieRepository = movieRepository;
		this.tmdbService = tmdbService;
	}

	@GetMapping(value = "/status")
	Mono<Status> status() {
		Mono<Status> last = tmdbService.getData("/status", Status.class).last();
		return last.zipWith(movieRepository.count(), (tmdbServiceResult, count) -> {
			double percentage = ((double) count / (double) tmdbServiceResult.total) * 100;
			log.info("Count: {}, Total: {}, Percentage: {}", count, tmdbServiceResult.total, percentage);
			return new Status(tmdbServiceResult.fetched, count, percentage);
		});
	}

	@GetMapping(value = "/view/best/{countryCode}")
	Flux<Movie> newMovie(@PathVariable String countryCode) {
		return movieRepository.findBestByProducerCountry(countryCode)
				.map(a -> Movie.builder()
						.movieId(a.getMovieId())
						.engTitle(a.getEngTitle())
						.originalTitle(a.getOriginalTitle())
						.posterPath(a.getPosterPath())
						.build());
	}

	public record Status(long total, long fetched, double percentageDone) {
	}
}

