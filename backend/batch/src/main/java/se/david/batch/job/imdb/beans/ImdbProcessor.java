package se.david.batch.job.imdb.beans;

import lombok.extern.java.Log;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.david.batch.job.country.CountryRepository;
import se.david.commons.Movie;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Log
public class ImdbProcessor implements ItemProcessor<String, Movie> {
    private static final String ULTIMATE_REGEX = "\"?(.*?)\"?\\s+\\(([0-9?]{4})\\)?.*\\s([\\w \\.\\-\\(\\)]+)[\\s]*$";
    private static final Pattern ULTIMATE_PATTERN = Pattern.compile(ULTIMATE_REGEX);

    @Override
    public Movie process(String string) throws Exception {
        Movie movie = null;

        Matcher matcher = ULTIMATE_PATTERN.matcher(string);
        if(matcher.matches()) {
            log.log(Level.FINE, "Matched: " + string);
            movie = new Movie();
            movie.setName(matcher.group(1));
            movie.setYear(matcher.group(2));
            movie.setCountry(MapConverter.countryCode(matcher.group(3)));
            movie.setId(movie.getName() + ":" + movie.getYear() + ":" + movie.getCountry());
        } else {
            log.log(Level.INFO, "No Matched: " + string);
        }

        return movie;
    }
}

