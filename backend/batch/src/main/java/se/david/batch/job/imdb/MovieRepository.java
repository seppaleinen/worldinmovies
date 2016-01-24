package se.david.batch.job.imdb;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import se.david.commons.Movie;

@Repository
public interface MovieRepository extends MongoRepository<Movie, String> {
    Movie findByNameAndYear(String name, String year);
}
