package se.david.backend.controllers.services;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.david.backend.controllers.repository.MovieRepository;
import se.david.commons.Movie;

import java.util.List;

@Service
@Log
public class ImdbMoviesByCountryService {
    @Autowired
    private MovieRepository movieRepository;
    private static final int MAX_RESULT = 5;

    public List<Movie> getMoviesByCountry(String country) {
        return movieRepository.findMovieByCountry(country, MAX_RESULT);
    }
}
