package se.david.backend.controllers;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.david.backend.WorldInMoviesApplication;
import se.david.backend.controllers.repository.MovieRepository;
import se.david.backend.controllers.repository.entities.Movie;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(
        classes = WorldInMoviesApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "classpath:application-test.properties")
public class ImdbControllerIT {
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
    public void testGetMoviesByCountry() {
        Movie movie = Movie.builder().
                id("ID").
                name("NAME").
                country("SE").
                build();

        movieRepository.save(movie);

        given().param("country", "SE").
                when().post(ImdbController.MOVIES_BY_COUNTRY).
                then().statusCode(HttpStatus.METHOD_NOT_ALLOWED.value());
        given().param("country", "SE").
                when().put(ImdbController.MOVIES_BY_COUNTRY).
                then().statusCode(HttpStatus.METHOD_NOT_ALLOWED.value());
        given().param("country", "SE").
                when().delete(ImdbController.MOVIES_BY_COUNTRY).
                then().statusCode(HttpStatus.METHOD_NOT_ALLOWED.value());
        given().param("country", "SE").
                when().patch(ImdbController.MOVIES_BY_COUNTRY).
                then().statusCode(HttpStatus.METHOD_NOT_ALLOWED.value());

        Response response = given().param("country", "SE").
                when().get(ImdbController.MOVIES_BY_COUNTRY);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<Movie> resultList = Arrays.asList(response.getBody().as(Movie[].class));
        assertNotNull(resultList);
        assertEquals(1, resultList.size());
        assertEquals("ID", resultList.get(0).getId());
        assertEquals("NAME", resultList.get(0).getName());
        assertEquals("SE", resultList.get(0).getCountry());
    }

    @Test
    public void testParseInput() throws IOException {
        Movie movie1 = Movie.builder().
                name("Time of the Wolf").
                year("2003").
                country("country").
                id("Time of the Wolf" + ":" + "2003" + ":country").
                build();
        movieRepository.save(movie1);

        File file = new ClassPathResource("small_ratings.csv").getFile();

        Response response = given().multiPart(file).when().post(ImdbController.USER_RATINGS_URL);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<Movie> result = Arrays.asList(response.getBody().as(Movie[].class));

        assertNotNull(result);
        assertEquals(1, result.size());

        assertEquals("Time of the Wolf", result.get(0).getName());
        assertEquals("2003", result.get(0).getYear());
        assertEquals("country", result.get(0).getCountry());

    }
}
