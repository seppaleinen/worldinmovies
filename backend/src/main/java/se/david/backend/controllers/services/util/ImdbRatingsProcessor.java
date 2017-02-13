package se.david.backend.controllers.services.util;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.david.backend.repository.MovieRepository;
import se.david.backend.domain.Movie;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Log
public class ImdbRatingsProcessor {
    private static final String RATINGS_REGEX = "^.*[\\d\\.\\*]+\\s+([\\d\\.]+)[\\s]+([\\d]+\\.\\d)[\\s]+([^\"].*[^\"])\\s\\(([\\d\\?/IVX]{4,})\\).*$";
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
            movie = movieRepository.findOne(matcher.group(3) + ":" + matcher.group(4));
            if(movie != null) {
                movie.setVotes(matcher.group(1));
                movie.setRating(matcher.group(2));
                try {
                    movie.setWeightedRating(evaluate(Double.valueOf(movie.getVotes()), Double.valueOf(movie.getRating())));
                } catch (NumberFormatException e) {
                    log.info(string);
                }
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

    private String evaluate(double votes, Double avgRating) {
        double weightedRating = (votes / (votes + 350)) * avgRating;
        return String.valueOf(weightedRating);
    }

}
