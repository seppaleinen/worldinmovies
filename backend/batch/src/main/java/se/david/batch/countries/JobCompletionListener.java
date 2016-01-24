package se.david.batch.countries;

import lombok.extern.java.Log;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.david.commons.Country;

import java.util.List;

@Component
@Log
public class JobCompletionListener extends JobExecutionListenerSupport {
    @Autowired
    private CountryRepository countryRepository;

    @Override
    public void afterJob(JobExecution jobExecution) {
        if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
            List<Country> resultList = countryRepository.findAll();

            log.info("Found: " + resultList.size() + " Entities in country collection");
        }
    }
}