package se.david.batch.job;

import lombok.extern.java.Log;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.david.batch.job.imdb.MovieRepository;
import se.david.batch.job.country.CountryRepository;
import se.david.commons.Country;
import se.david.commons.Movie;

import java.util.List;

@Component
@Log
public class BatchCompletionListener extends JobExecutionListenerSupport {
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private MovieRepository movieRepository;

    @Override
    public void afterJob(JobExecution jobExecution) {
        if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
            List<Country> resultList = countryRepository.findAll();

            log.info("Found: " + resultList.size() + " job found");

            List<Movie> movieList = movieRepository.findAll();

            log.info("Found: " + movieList.size() + " movies found");
        }
    }
}