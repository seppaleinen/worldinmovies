package se.david.backend.controllers.services;

import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import se.david.backend.controllers.repository.MovieRepository;
import se.david.commons.Movie;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Takes file as argument from imdb ratings export function (http://www.imdb.com/list/export?list_id=ratings&author_id=ur32409321)
 * and connects the movies to movieEntity from user
 * 1. Read files
 * 2. Skip first line which is information
 * 3. Split on ','
 * 4. column 5 is moviename
 * 5. column 11 is year
 * 6. find corresponding movie from database
 * 7. attach list of movies to user
 */
@Service
@Log
public class ImdbUserRatingsService {
    @Autowired
    private MovieRepository movieRepository;

    public List<Movie> parseFromUserRatingsFile(MultipartFile file){
        List<Movie> movieEntityList = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));

            List<String> idList = new ArrayList<>();

            for(String line : br.lines().skip(1).collect(Collectors.toList())) {
                log.log(Level.FINE, "Reading line: " + line);
                String[] split = line.split(",");

                String movieName = split[5].replaceAll("\"", "");
                String movieYear = split[11].replaceAll("\"", "");

                idList.add("^" + movieName + ":" + movieYear + ":");
            }

            movieEntityList = movieRepository.findByIdMultiple(idList);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return movieEntityList;
    }
}
