package se.david.batch.imdb;

import lombok.extern.java.Log;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.david.batch.countries.CountryRepository;
import se.david.commons.Country;
import se.david.commons.Movie;

import java.util.List;

@Component(value = "ImdbBatchListener")
@Log
public class ImdbBatchCompletionListener extends JobExecutionListenerSupport {
    @Autowired
    private MovieRepository movieRepository;

    @Override
    public void afterJob(JobExecution jobExecution) {
        if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
            List<Movie> resultList = movieRepository.findAll();

            log.info("Found: " + resultList.size() + " Entities in movie collection");
        }
    }
}