package se.david.backend.repository;

import com.jayway.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import se.david.backend.WorldInMoviesApplication;
import se.david.backend.domain.Movie;

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
@TestPropertySource(locations="classpath:application-test.properties")
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
    public void test_findTop5ByCountry_Expect5_OutOf6_AndOrderOfRatings() {
        Movie movie1 = Movie.builder().id("name:1").countrySet(Sets.newSet("SE")).rating("1.1").weightedRating("1.1").build();
        Movie movie2 = Movie.builder().id("name:2").countrySet(Sets.newSet("SE")).rating("2.3").weightedRating("2.3").build();
        Movie movie3 = Movie.builder().id("name:3").countrySet(Sets.newSet("SE")).rating("3.4").weightedRating("3.4").build();
        Movie movie4 = Movie.builder().id("name:4").countrySet(Sets.newSet("SE")).rating("4.5").weightedRating("4.5").build();
        Movie movie5 = Movie.builder().id("name:5").countrySet(Sets.newSet("SE")).rating("5.5").weightedRating("5.5").build();
        Movie movie6 = Movie.builder().id("name:6").countrySet(Sets.newSet("SE")).build();

        movieRepository.save(Arrays.asList(movie1, movie2, movie3, movie4, movie5, movie6));

        List<Movie> pageOne = movieRepository.findTop5ByCountrySetOrderByWeightedRatingDesc("SE", new PageRequest(0, 5));

        assertNotNull(pageOne);
        assertEquals(5, pageOne.size());
        assertEquals(movie5, pageOne.get(0));
        assertEquals(movie4, pageOne.get(1));
        assertEquals(movie3, pageOne.get(2));
        assertEquals(movie2, pageOne.get(3));
        assertEquals(movie1, pageOne.get(4));

        List<Movie> pageTwo = movieRepository.findTop5ByCountrySetOrderByWeightedRatingDesc("SE", new PageRequest(1, 5));

        assertNotNull(pageTwo);
        assertEquals(1, pageTwo.size());
        assertTrue(pageTwo.contains(movie6));
    }

    @Test
    public void test_findTop5ByCountry_Expect1() {
        Movie movie1 = Movie.builder().id("name:1").countrySet(Sets.newSet("SE")).rating("1.1").weightedRating("1.1").build();
        Movie movie2 = Movie.builder().id("name:2").countrySet(Sets.newSet("ES")).rating("1.2").weightedRating("1.2").build();
        Movie movie3 = Movie.builder().id("name:3").countrySet(Sets.newSet("MX")).rating("2.3").weightedRating("2.3").build();
        Movie movie4 = Movie.builder().id("name:4").countrySet(Sets.newSet("US")).rating("9.7").weightedRating("9.7").build();
        Movie movie5 = Movie.builder().id("name:5").countrySet(Sets.newSet("BE")).rating("9.8").weightedRating("9.8").build();
        Movie movie6 = Movie.builder().id("name:6").countrySet(Sets.newSet("BG")).build();

        movieRepository.save(Arrays.asList(movie1, movie2, movie3, movie4, movie5, movie6));

        List<Movie> result = movieRepository.findTop5ByCountrySetOrderByWeightedRatingDesc("SE", new PageRequest(0, 5));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(movie1.getId(), result.get(0).getId());
        assertEquals(movie1, result.get(0));
    }

    @Test
    public void test_findTop5ByCountry_ExpectRating() {
        Movie movie1 = Movie.builder().id("name:1").countrySet(Sets.newSet("SE")).rating("1.1").weightedRating("1.1").build();
        Movie movie2 = Movie.builder().id("name:2").countrySet(Sets.newSet("ES")).rating("1.2").weightedRating("1.2").build();
        Movie movie3 = Movie.builder().id("name:3").countrySet(Sets.newSet("MX")).rating("2.3").weightedRating("2.3").build();
        Movie movie4 = Movie.builder().id("name:4").countrySet(Sets.newSet("US")).rating("9.7").weightedRating("9.7").build();
        Movie movie5 = Movie.builder().id("name:5").countrySet(Sets.newSet("BE")).rating("9.8").weightedRating("9.8").build();
        Movie movie6 = Movie.builder().id("name:6").countrySet(Sets.newSet("BG")).build();

        movieRepository.save(Arrays.asList(movie1, movie2, movie3, movie4, movie5, movie6));

        List<Movie> result = movieRepository.findTop5ByCountrySetOrderByWeightedRatingDesc("SE", new PageRequest(0, 5));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(movie1.getId(), result.get(0).getId());
        assertEquals(movie1, result.get(0));
    }
}
