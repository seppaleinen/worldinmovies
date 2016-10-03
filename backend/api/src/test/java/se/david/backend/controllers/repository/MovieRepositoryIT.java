package se.david.backend.controllers.repository;

import com.jayway.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import se.david.backend.WorldInMoviesApplication;
import se.david.backend.controllers.repository.entities.Movie;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = WorldInMoviesApplication.class, properties = "classpath:application-test.properties", webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MovieRepositoryIT {
    @Autowired
    private MovieRepository movieRepository;

    @LocalServerPort
    private int port;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        //ReflectionTestUtils.setField(callbackService, "restTemplate", restTemplateMock);
        movieRepository.deleteAll();
        RestAssured.port = port;
    }

    @Test
    public void expect2Results_WithWildcard() {
        Movie movie1 = new Movie();
        movie1.setId("name:year:1");

        Movie movie2 = new Movie();
        movie2.setId("name:year:2");

        movieRepository.save(movie1);
        movieRepository.save(movie2);

        List<Movie> result = movieRepository.findByIdMultiple(Arrays.asList("name:year:"));

        assertNotNull("Result should not be null", result);
        assertEquals(2, result.size());
    }

    @Test
    public void expect1Result_With1ExactMatches() {
        Movie movie1 = new Movie();
        movie1.setId("name:year:1");

        Movie movie2 = new Movie();
        movie2.setId("name:year:2");

        movieRepository.save(movie1);
        movieRepository.save(movie2);

        List<Movie> result = movieRepository.findByIdMultiple(Arrays.asList("name:year:1"));

        assertNotNull("Result should not be null", result);
        assertEquals(1, result.size());
    }

    @Test
    public void expect2Results_With_2ExactMatches() {
        Movie movie1 = new Movie();
        movie1.setId("othername:otheryear:SE");

        Movie movie2 = new Movie();
        movie2.setId("name:year:IT");

        movieRepository.save(movie1);
        movieRepository.save(movie2);

        List<Movie> result = movieRepository.findByIdMultiple(Arrays.asList(
                "^" + movie1.getId(),
                "^" + movie2.getId()));

        assertNotNull("Result should not be null", result);
        assertEquals(2, result.size());
    }


}
