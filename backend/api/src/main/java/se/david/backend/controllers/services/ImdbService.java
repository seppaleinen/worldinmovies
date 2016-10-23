package se.david.backend.controllers.services;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.google.common.collect.Lists;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import se.david.backend.controllers.repository.MovieRepository;
import se.david.backend.controllers.repository.UserRepository;
import se.david.backend.controllers.repository.entities.Movie;
import se.david.backend.controllers.repository.entities.User;

import javax.validation.ValidationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Takes file as argument from imdb ratings export function (http://www.imdb.com/list/export?list_id=ratings&author_id=ur32409321)
 * and connects the movies to movieEntity from user
 * 1. column 5 is moviename
 * 2. column 11 is year
 * 3. find corresponding movie from database
 * 4. attach list of movies to user
 */
@Service
@Log
public class ImdbService {
    private static final int MAX_RESULT = 5;

    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private UserRepository userRepository;

    public List<Movie> parseFromUserRatingsFile(MultipartFile file, String username){
        List<Movie> movieList = new ArrayList<>();

        try {
            List<String> idList = new ArrayList<>();

            CsvMapper csvMapper = new CsvMapper();
            csvMapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
            MappingIterator<String[]> it = csvMapper.readerFor(String[].class).
                    readValues(file.getInputStream());
            it.readAll().
                    stream().
                    skip(1).
                    forEach(row -> addToList(idList, row));

            movieList = Lists.newArrayList(movieRepository.findAll(idList)).stream().filter(movie -> movie != null).collect(Collectors.toList());

            User user = userRepository.findOne(username);

            if(user != null) {
                Set<Movie> hashSet = new HashSet<>(movieList);
                hashSet.addAll(user.getMovies() == null ? new ArrayList<>() : user.getMovies());
                movieList = new ArrayList<>(hashSet).stream().filter(movie -> movie != null).collect(Collectors.toList());
                user.setMovies(movieList);
                userRepository.save(user);
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }

        return movieList;
    }

    private void addToList(List<String> idList, String[] row) {
        if(row.length > 11) {
            idList.add(row[5] + ":" + row[11]);
        }
    }

    public List<Movie> getMoviesByCountry(String country) {
        return movieRepository.findTop5ByCountrySetOrderByWeightedRatingDesc(country, new PageRequest(0, MAX_RESULT));
    }
}
