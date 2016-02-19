package se.david.batch.job;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.david.batch.WorldInMoviesBatchApplication;
import se.david.batch.job.country.CountryRepository;
import se.david.batch.job.imdb.MovieRepository;
import se.david.commons.Country;
import se.david.commons.Movie;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WorldInMoviesBatchApplication.class})
public class BatchIT {
    @Autowired
    private MovieRepository repository;
    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private Job job;
    @Value("file:src/test/resources/difficult_countries.list")
    private Resource countriesResource;

    @Before
    public void setup() {
        repository.deleteAll();
    }

    @Ignore
    @Test
    public void canFindMapByGet() throws IOException, JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        jobLauncher.run(job, new JobParametersBuilder().addString("input", countriesResource.getFile().getAbsolutePath()).toJobParameters());

        Movie movie = repository.findByNameAndYear("Chas oborotnya", "1990");
        assertNotNull(movie);
    }
}
