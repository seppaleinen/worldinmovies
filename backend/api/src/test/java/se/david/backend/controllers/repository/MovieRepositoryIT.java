package se.david.backend.controllers.repository;

import com.jayway.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import se.david.backend.WorldInMoviesApplication;
import se.david.backend.controllers.repository.entities.Movie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = WorldInMoviesApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "classpath:application-test.properties")
@ContextConfiguration(classes = WorldInMoviesApplication.class)
public class MovieRepositoryIT {
    @Autowired
    private MovieRepository movieRepository;

    @LocalServerPort
    private int port;

    @Before
    public void setup() {
        movieRepository.deleteAll();
        RestAssured.port = port;
    }

    @Test
    public void expect2Results_WithWildcard() {
        Movie movie1 = Movie.builder().id("name:year:1").build();
        Movie movie2 = Movie.builder().id("name:year:2").build();

        movieRepository.save(Arrays.asList(movie1, movie2));

        List<Movie> result = movieRepository.findByIdRegex("name:year:");

        assertNotNull("Result should not be null", result);
        assertEquals(2, result.size());

    }

    @Test
    public void expect1Result_With1ExactMatches() {
        Movie movie1 = Movie.builder().id("name:year:1").build();
        Movie movie2 = Movie.builder().id("name:year:2").build();

        movieRepository.save(Arrays.asList(movie1, movie2));

        List<Movie> result = movieRepository.findByIdRegex("name:year:1");

        assertNotNull("Result should not be null", result);
        assertEquals(1, result.size());
    }

    @Test
    public void expect2Results_With_2ExactMatches() {
        Movie movie1 = Movie.builder().id("othername:otheryear:SE").build();
        Movie movie2 = Movie.builder().id("name:year:IT").build();

        movieRepository.save(Arrays.asList(movie1, movie2));

        List<Movie> result = new ArrayList<>();
        for(String id: Arrays.asList(movie1.getId(), movie2.getId())) {
            result.addAll(movieRepository.findByIdRegex(id));
        }

        assertNotNull("Result should not be null", result);
        assertEquals(2, result.size());
    }

    @Test
    public void test_findTop5ByCountry_Expect5_OutOf6() {
        Movie movie1 = Movie.builder().id("name:1:SE").country("SE").build();
        Movie movie2 = Movie.builder().id("name:2:SE").country("SE").build();
        Movie movie3 = Movie.builder().id("name:3:SE").country("SE").build();
        Movie movie4 = Movie.builder().id("name:4:SE").country("SE").build();
        Movie movie5 = Movie.builder().id("name:5:SE").country("SE").build();
        Movie movie6 = Movie.builder().id("name:6:SE").country("SE").build();

        movieRepository.save(Arrays.asList(movie1, movie2, movie3, movie4, movie5, movie6));

        List<Movie> pageOne = movieRepository.findTop5ByCountry("SE", new PageRequest(0, 5));

        assertNotNull(pageOne);
        assertEquals(5, pageOne.size());
        assertTrue(pageOne.contains(movie1));
        assertTrue(pageOne.contains(movie2));
        assertTrue(pageOne.contains(movie3));
        assertTrue(pageOne.contains(movie4));
        assertTrue(pageOne.contains(movie5));


        List<Movie> pageTwo = movieRepository.findTop5ByCountry("SE", new PageRequest(1, 5));

        assertNotNull(pageTwo);
        assertEquals(1, pageTwo.size());
        assertTrue(pageTwo.contains(movie6));
    }

    @Test
    public void test_findTop5ByCountry_Expect1() {
        Movie movie1 = Movie.builder().id("name:1:SE").country("SE").build();
        Movie movie2 = Movie.builder().id("name:2:ES").country("ES").build();
        Movie movie3 = Movie.builder().id("name:3:MX").country("MX").build();
        Movie movie4 = Movie.builder().id("name:4:US").country("US").build();
        Movie movie5 = Movie.builder().id("name:5:BE").country("BE").build();
        Movie movie6 = Movie.builder().id("name:6:BG").country("BG").build();

        movieRepository.save(Arrays.asList(movie1, movie2, movie3, movie4, movie5, movie6));

        List<Movie> result = movieRepository.findTop5ByCountry("SE", new PageRequest(0, 5));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(movie1.getId(), result.get(0).getId());
        assertTrue(result.contains(movie1));
    }
}
