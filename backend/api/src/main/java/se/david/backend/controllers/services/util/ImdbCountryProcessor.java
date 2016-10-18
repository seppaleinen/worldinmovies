package se.david.backend.controllers.services.util;

import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import se.david.backend.controllers.repository.entities.Movie;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Log
public class ImdbCountryProcessor {
    private static final String COUNTRY_REGEX = "^([^\"]*?)\\\"?\\s+\\(([\\d\\?A-Z\\/]{4,8})\\)?.*\\t([\\w\\ \\.\\-\\(\\)]+)\\s?$";
    private static final Pattern COUNTRY_PATTERN = Pattern.compile(COUNTRY_REGEX);

    public Set<Movie> process(List<String> rowList) {
        return rowList.stream().map(this::process).filter(movie -> movie != null).collect(Collectors.toSet());
    }

    Movie process(String string) {
        Movie movie = null;

        Matcher matcher = COUNTRY_PATTERN.matcher(string);
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
                movie = null;
            }
        }

        return movie;
    }

}
