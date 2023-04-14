package se.worldinmovies.neo4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.worldinmovies.neo4j.domain.Movie;
import se.worldinmovies.neo4j.entity.MovieEntity;
import se.worldinmovies.neo4j.repository.MovieRepository;

@RestController
public class Controller {
	private final MovieRepository movieRepository;

	public Controller(MovieRepository movieRepository) {
		this.movieRepository = movieRepository;
	}

	@GetMapping(value = "/status")
	Flux<MovieEntity> status() {
		return movieRepository.findAll();
	}

	@GetMapping(value = "/hej")
	Mono<MovieEntity> newMovie() {
		return movieRepository.save(new MovieEntity(Movie.builder()
				.movieId(1)
				.build()));
	}
}
