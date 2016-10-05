package se.david.backend.controllers.services;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import se.david.backend.controllers.repository.MovieRepository;
import se.david.backend.controllers.repository.entities.Movie;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

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

    public List<Movie> parseFromUserRatingsFile(MultipartFile file){
        List<Movie> movieEntityList = new ArrayList<>();

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

            movieEntityList = movieRepository.findByIdMultiple(idList);
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }

        return movieEntityList;
    }

    private void addToList(List<String> idList, String[] row) {
        if(row.length > 11) {
            idList.add("^" + row[5] + ":" + row[11] + ":");
        }
    }


    public List<Movie> getMoviesByCountry(String country) {
        return movieRepository.findMovieByCountry(country, MAX_RESULT);
    }
}
