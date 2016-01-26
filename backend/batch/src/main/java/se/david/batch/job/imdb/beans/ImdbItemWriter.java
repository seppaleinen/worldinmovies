package se.david.batch.job.imdb.beans;

import lombok.extern.java.Log;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.david.batch.job.imdb.MovieRepository;
import se.david.commons.Movie;

import java.util.List;
import java.util.logging.Level;

@Service
@Log
public class ImdbItemWriter implements ItemWriter<Movie> {
    @Autowired
    private MovieRepository movieRepository;

    @Override
    public void write(List<? extends Movie> list) throws Exception {
        for(Movie movie : list) {
            if(movieRepository.findByNameAndYear(movie.getName(), movie.getYear()) == null) {
                log.log(Level.FINE, "Saving new movie: " + movie.getName());
                movieRepository.save(movie);
            }
        }
    }
}
