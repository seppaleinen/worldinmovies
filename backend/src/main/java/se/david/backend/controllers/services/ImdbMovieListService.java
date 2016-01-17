package se.david.backend.controllers.services;

import se.david.backend.controllers.repository.entities.CountryEntity;
import se.david.backend.controllers.repository.entities.MovieEntity;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 1. Download countries.list.gz (ftp://ftp.sunet.se/pub/tv+movies/imdb/countries.list.gz)
 * 2. Unzip
 * 3. Parse all movies to MovieEntity
 * 4. Persist list of movies
 */
public class ImdbMovieListService {
    private static final String regexNameAndYear = "\"?(.*?)\"?\\s+\\(([0-9?]{4})\\)?";
    private static final String regexCountry = "\\t([\\w \\.\\-\\(\\)]+)[\\s]*$";

    private static final Pattern patternNameAndYear = Pattern.compile(regexNameAndYear);
    private static final Pattern patternCountry = Pattern.compile(regexCountry);

    List<MovieEntity> parseImdbMovieList(URL url) throws Exception {
        List<MovieEntity> movieEntityList = new ArrayList<>();

        if(url != null) {
            try {
                Stream<String> result = Files.lines(Paths.get(url.getPath()), StandardCharsets.ISO_8859_1);

                for (String string : result.skip(16).collect(Collectors.toList())) {
                    if(!string.equals("--------------------------------------------------------------------------------")) {
                        MovieEntity movieEntity = parseMovieEntity(string);
                        movieEntityList.add(movieEntity);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return movieEntityList;
    }

    MovieEntity parseMovieEntity(String line) throws Exception {
        MovieEntity movieEntity = new MovieEntity();

        Matcher nameAndYearMatcher = patternNameAndYear.matcher(line);

        if(nameAndYearMatcher.find()) {
            movieEntity.setName(nameAndYearMatcher.group(1));
            movieEntity.setYear(nameAndYearMatcher.group(2));

            Matcher countryMatcher = patternCountry.matcher(line);

            if(countryMatcher.find()) {
                CountryEntity countryEntity = new CountryEntity();
                countryEntity.setName(countryMatcher.group(0).trim());
                movieEntity.setCountryEntity(countryEntity);
            } else {
                throw new Exception("Coult not parse country: " + line);
            }
        } else {
            throw new Exception("Could not parse line " + line);
        }

        return movieEntity;
    }

    public void mapCountries() {
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
    }

}
