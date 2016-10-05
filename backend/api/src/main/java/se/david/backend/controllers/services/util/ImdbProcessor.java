package se.david.backend.controllers.services.util;

import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import se.david.backend.controllers.repository.entities.Movie;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Log
public class ImdbProcessor {
    private static final String ULTIMATE_REGEX = "^([^\"]*?)\\\"?\\s+\\(([\\d\\?A-Z\\/]{4,8})\\)?.*\\t([\\w\\ \\.\\-\\(\\)]+)\\s?$";
    private static final Pattern ULTIMATE_PATTERN = Pattern.compile(ULTIMATE_REGEX);

    public Set<Movie> process(List<String> string) {
        Set<Movie> movieList = new HashSet<>();
        string.forEach(row -> movieList.add(process(row)));
        return movieList;
    }

    public Movie process(String string) {
        Movie movie = null;

        Matcher matcher = ULTIMATE_PATTERN.matcher(string);
        if(matcher.matches()) {
            log.log(Level.FINE, "Matched: " + string);
            movie = Movie.builder().
                    name(matcher.group(1)).
                    year(matcher.group(2)).
                    country(MapConverter.countryCode(matcher.group(3))).
                    build();
            movie.setId(movie.getName() + ":" + movie.getYear() + ":" + movie.getCountry());
            if(movie.getName() == null ||
               movie.getYear() == null ||
               movie.getCountry() == null ||
               movie.getId() == null) {
                log.log(Level.INFO, "Movie missing values: " + string);
                return null;
            }
        }

        return movie;
    }

}
