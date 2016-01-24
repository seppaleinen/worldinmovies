package se.david.batch.job.imdb.beans;

import lombok.extern.java.Log;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Service;
import se.david.commons.Movie;

@Service
@Log
public class ImdbProcessor implements ItemProcessor<Movie, Movie> {
    @Override
    public Movie process(Movie movie) throws Exception {
        //log.log(Level.INFO, "Processing: " + countryEntity);
        return movie;
    }
}

