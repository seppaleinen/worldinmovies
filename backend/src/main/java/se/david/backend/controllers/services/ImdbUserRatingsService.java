package se.david.backend.controllers.services;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import se.david.backend.controllers.repository.CountryRepository;
import se.david.backend.controllers.repository.MovieRepository;
import se.david.backend.controllers.repository.entities.CountryEntity;
import se.david.backend.controllers.repository.entities.MovieEntity;

import java.io.*;
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

    public List<MovieEntity> parseFromUserRatingsFile(MultipartFile file){
        List<MovieEntity> movieEntityList = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));

            log.log(Level.INFO, "READ LINES");
            for(String line : br.lines().skip(1).collect(Collectors.toList())) {
                log.log(Level.INFO, "READ LINE " + line);
                String[] split = line.split(",");

                String movieName = split[5].replaceAll("\"", "");
                String movieYear = split[11].replaceAll("\"", "");

                // MovieEntity movieEntity = movieRepository.findByNameAndYear(movieName, movieYear);
                MovieEntity movieEntity = new MovieEntity();
                movieEntity.setName(movieName);
                movieEntity.setYear(movieYear);

                movieEntityList.add(movieEntity);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.log(Level.INFO, "returning " + movieEntityList.size() + movieEntityList);

        return movieEntityList;
    }

    String mapCountries(String imdbCountryName) {
        Map<String, String> specialCountries = new HashMap<>();
        specialCountries.put("Netherlands Antilles",    "Netherlands");
        specialCountries.put("Burma",                   "Myanmar");
        specialCountries.put("Ivory Coast",             "Côte d'Ivoire");
        specialCountries.put("Czechoslovakia",          "Czech Republic");
        specialCountries.put("Kosovo",                  "Serbia");
        specialCountries.put("Laos",                    "Lao People's Democratic Republic");
        specialCountries.put("Reunion",                 "Réunion");
        specialCountries.put("Siam",                    "Thailand");
        specialCountries.put("UK",                      "United Kingdom");
        specialCountries.put("USA",                     "United States");
        specialCountries.put("Soviet Union",            "Russian Federation");
        specialCountries.put("Vietnam",                 "Viet nam");
        specialCountries.put("Yugoslavia",              "Serbia");
        specialCountries.put("Zaire",                   "Congo, the Democratic Republic of the");

        String regularCountryName = specialCountries.get(imdbCountryName);

        return regularCountryName != null ? regularCountryName : imdbCountryName;
    }

}
