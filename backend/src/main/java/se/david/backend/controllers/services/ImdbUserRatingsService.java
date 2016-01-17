package se.david.backend.controllers.services;

import org.springframework.stereotype.Service;
import se.david.backend.controllers.repository.entities.MovieEntity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
public class ImdbUserRatingsService {
    public List<MovieEntity> parseFromUserRatingsFile(File file){
        List<MovieEntity> movieEntityList = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            for(String line : br.lines().skip(1).collect(Collectors.toList())) {
                String[] split = line.split(",");
                MovieEntity movieEntity = new MovieEntity();
                movieEntity.setName(split[5].replaceAll("\"", ""));
                movieEntity.setYear(split[11].replaceAll("\"", ""));
                movieEntityList.add(movieEntity);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return movieEntityList;
    }
}
