package se.david.backend.controllers.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import se.david.backend.controllers.repository.entities.Movie;

import java.util.List;

@Repository
public interface MovieRepository extends MongoRepository<Movie, String> {
    List<Movie> findByIdRegex(String id);
    List<Movie> findTop5ByCountrySet(String country, Pageable pageable);
}
