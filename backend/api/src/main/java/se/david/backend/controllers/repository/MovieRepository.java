package se.david.backend.controllers.repository;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import se.david.backend.controllers.repository.entities.Movie;

import java.util.ArrayList;
import java.util.List;

@Repository
@Log
public class MovieRepository  {
    @Autowired
    private MongoOperations mongoOperations;

    public List<Movie> findByIdMultiple(List<String> idList) {
        List<Movie> movieList = new ArrayList<>();

        for(String id : idList) {
            Query query = new Query(Criteria.where("id").regex(id));
            movieList.addAll(mongoOperations.find(query, Movie.class));
        }

        return movieList;
    }

    public List<Movie> findMovieByCountry(String country, int maxResult) {
        Query query = new Query(Criteria.where("country").is(country));
        query.limit(maxResult);

        return mongoOperations.find(query, Movie.class);
    }

    public void deleteAll() {
        mongoOperations.dropCollection(Movie.class);
    }

    public void save(Movie movie) {
        mongoOperations.save(movie);
    }

    public void save(List<Movie> movieList) {
        mongoOperations.insertAll(movieList);
    }

    public long count() {
        return mongoOperations.count(new Query(), Movie.class);
    }
}
