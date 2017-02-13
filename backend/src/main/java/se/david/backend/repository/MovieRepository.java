package se.david.backend.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import se.david.backend.domain.Movie;

import java.util.List;

@Repository
public interface MovieRepository extends MongoRepository<Movie, String> {
    List<Movie> findByIdRegex(String id);
    List<Movie> findTop5ByCountrySetOrderByWeightedRatingDesc(String country, Pageable pageable);
}
