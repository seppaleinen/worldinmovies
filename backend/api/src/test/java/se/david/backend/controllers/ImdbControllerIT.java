package se.david.backend.controllers;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import se.david.backend.WorldInMoviesApplication;
import se.david.backend.controllers.repository.MovieRepository;
import se.david.backend.controllers.repository.UserRepository;
import se.david.backend.controllers.repository.entities.Movie;
import se.david.backend.controllers.repository.entities.User;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = WorldInMoviesApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class ImdbControllerIT {
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private UserRepository userRepository;
    @LocalServerPort
    private int port;

    @Before
    public void setup() {
        movieRepository.deleteAll();
        userRepository.deleteAll();
        RestAssured.port = port;
    }

    @Test
    public void testGetMoviesByCountry() {
        Movie movie = Movie.builder().
                id("ID").
                name("NAME").
                build();
        movie.setCountrySet(Sets.newSet("SE"));

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
        assertEquals(null, resultList.get(0).getId());
        assertEquals("NAME", resultList.get(0).getName());
        assertEquals("SE", resultList.get(0).getCountrySet().iterator().next());
    }

    @Test
    public void testParseInput_NoExistingUser() throws IOException {
        Movie movie1 = Movie.builder().
                name("Time of the Wolf").
                year("2003").
                id("Time of the Wolf" + ":" + "2003").
                build();
        movie1.setCountrySet(Sets.newSet("country"));
        movieRepository.save(movie1);

        File file = new ClassPathResource("small_ratings.csv").getFile();

        Response response = given().multiPart(file).param("username", "NON_EXISTING_USERNAME").when().post(ImdbController.USER_RATINGS_URL);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<Movie> result = Arrays.asList(response.getBody().as(Movie[].class));

        assertNotNull(result);
        assertEquals(1, result.size());

        assertEquals("Time of the Wolf", result.get(0).getName());
        assertEquals("2003", result.get(0).getYear());
        assertEquals("country", result.get(0).getCountrySet().iterator().next());
    }

    @Test
    public void testParseInput_NoExistingMovieInUser() throws IOException {
        Movie movie1 = Movie.builder().
                name("Time of the Wolf").
                year("2003").
                id("Time of the Wolf" + ":" + "2003").
                build();
        movie1.setCountrySet(Sets.newSet("country"));
        movieRepository.save(movie1);

        User user = User.builder().username("username").password("password").build();
        userRepository.save(user);

        File file = new ClassPathResource("small_ratings.csv").getFile();

        Response response = given().multiPart(file).param("username", user.getUsername()).when().post(ImdbController.USER_RATINGS_URL);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<Movie> result = Arrays.asList(response.getBody().as(Movie[].class));

        assertNotNull(result);
        assertEquals(1, result.size());

        assertEquals("Time of the Wolf", result.get(0).getName());
        assertEquals("2003", result.get(0).getYear());
        assertEquals("country", result.get(0).getCountrySet().iterator().next());

        List<Movie> moviesFromDatabase = userRepository.findOne(user.getUsername()).getMovies();
        for(Movie movie: moviesFromDatabase) {
            movie.setId(null);
        }
        assertEquals(result, moviesFromDatabase);
    }

    @Test
    public void testParseInput_WithExistingMoviesInUser() throws IOException {
        Movie movie1 = Movie.builder().
                name("Time of the Wolf").
                year("2003").
                id("Time of the Wolf:2003").
                build();
        movie1.setCountrySet(Sets.newSet("country"));
        movieRepository.save(movie1);

        Movie movie2 = Movie.builder().
                name("Dead End Drive-In").
                year("1986").
                id("Dead End Drive-In:1986").
                build();
        movieRepository.save(movie2);

        User user = User.builder().username("username").password("password").movies(Collections.singletonList(movie2)).build();
        userRepository.save(user);



        File file = new ClassPathResource("small_ratings.csv").getFile();

        Response response = given().multiPart(file).param("username", user.getUsername()).when().post(ImdbController.USER_RATINGS_URL);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<Movie> result = Arrays.asList(response.getBody().as(Movie[].class));

        assertNotNull(result);
        assertEquals(2, result.size());

        boolean movie1Found = false;
        boolean movie2Found = false;
        for(Movie movie: result) {
            if(movie1.getId().equals(movie.getName() + ":" + movie.getYear())) {
                movie1Found = true;
            } else if(movie2.getId().equals(movie.getName() + ":" + movie.getYear())) {
                movie2Found = true;
            }
        }
        assertTrue(movie1Found);
        assertTrue(movie2Found);

        List<Movie> moviesFromDatabase = userRepository.findOne(user.getUsername()).getMovies();
        for(Movie movie: moviesFromDatabase) {
            movie.setId(null);
        }
        assertEquals(result, moviesFromDatabase);
    }
}
