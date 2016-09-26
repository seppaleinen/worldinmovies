package se.david.batch.job;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import se.david.batch.WorldInMoviesBatchApplication;
import se.david.batch.job.imdb.MovieRepository;
import se.david.batch.job.imdb.MovieStep;
import se.david.batch.job.imdb.beans.ImdbItemReaderHelper;
import se.david.commons.Movie;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WorldInMoviesBatchApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "classpath:application-test.properties")
public class BatchIT {
    @Autowired
    private MovieRepository repository;
    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private Job job;
    @Value("file:src/test/resources/difficult_countries.list")
    private Resource countriesResource;
    @Mock
    private ImdbItemReaderHelper imdbItemReaderHelper;
    @Autowired
    private MovieStep movieStep;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(imdbItemReaderHelper.getResource()).thenReturn(countriesResource);
        ReflectionTestUtils.setField(movieStep, "imdbItemReaderHelper", imdbItemReaderHelper);
        repository.deleteAll();
    }

    @Ignore
    @Test
    public void canFindMapByGet() throws IOException, JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        jobLauncher.run(job, new JobParametersBuilder().addString("input", countriesResource.getFile().getAbsolutePath()).toJobParameters());

        Movie movie = repository.findByNameAndYear("Chas oborotnya", "1990");
        assertNotNull(movie);
    }

    @Test
    public void canStartServer() {
        assertTrue(true);
    }
}
