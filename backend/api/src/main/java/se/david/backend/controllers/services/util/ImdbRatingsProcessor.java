package se.david.backend.controllers.services.util;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.david.backend.controllers.repository.MovieRepository;
import se.david.backend.controllers.repository.entities.Movie;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Log
public class ImdbRatingsProcessor {
    private static final String RATINGS_REGEX = "^.*[\\d\\.\\*]+\\s+[\\d\\.]+\\s+([\\d\\.]+)\\s+([^\"].*[^\"])\\s\\(([\\d\\?/IVX]{4,})\\).*$";
    //private static final String RATINGS_REGEX = "^.*[\\d\\.\\*]+\\s+([\\d\\.]+)\\s+([\\d\\.]+)\\s+([^\"].*[^\"])\\s\\(([\\d\\?/IVX]{4,})\\).*$";
    private static final Pattern RATINGS_PATTERN = Pattern.compile(RATINGS_REGEX);

    @Autowired
    private MovieRepository movieRepository;

    public Set<Movie> process(List<String> rowList) {
        return rowList.stream().map(this::process).filter(movie -> movie != null).collect(Collectors.toSet());
    }

    Movie process(String string) {
        Movie movie = null;

        Matcher matcher = RATINGS_PATTERN.matcher(string);
        if(matcher.matches()) {
            movie = movieRepository.findOne(matcher.group(2) + ":" + matcher.group(3));
            if(movie != null) {
                movie.setRating(matcher.group(1));
                if (movie.getName() == null ||
                        movie.getYear() == null ||
                        movie.getId() == null ||
                        movie.getRating() == null) {
                    log.log(Level.INFO, "Movie missing values: " + string);
                    movie = null;
                }
            }
        }

        return movie;
    }

}
