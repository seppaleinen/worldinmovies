package se.david.backend.controllers;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.multipart.MultipartFile;
import se.david.backend.WorldInMoviesApplication;
import se.david.backend.controllers.repository.MovieRepository;
import se.david.backend.controllers.repository.entities.Movie;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {WorldInMoviesApplication.class})
// NOTE!! order is important
@WebAppConfiguration
@IntegrationTest("server.port:0")
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class ImdbControllerIT {
    @Autowired
    private MovieRepository movieRepository;
    @Value("${local.server.port}")
    private int port;

    @Before
    public void setup() {
        RestAssured.port = port;
    }

    @Test
    public void testGetMoviesByCountry() {
        Movie movie = new Movie();
        movie.setId("ID");
        movie.setName("NAME");
        movie.setCountry("SE");

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
        List<Movie> resultList = response.getBody().as(List.class);
        assertNotNull(resultList);
        assertFalse(resultList.isEmpty());
        //assertEquals("ID", resultList.get(0).getId());
    }

    @Ignore
    @Test
    public void testParseInput() throws IOException {
        Movie movieEntity = new Movie();
        movieEntity.setName("Time of the Wolf");
        movieEntity.setYear("2003");
        movieEntity.setCountry("country");
        movieEntity.setId(movieEntity.getName() + ":" + movieEntity.getYear() + ":country");
        movieRepository.save(movieEntity);

        String path = ImdbControllerIT.class.getClassLoader().getResource("small_ratings.csv").getPath();
        File file = new File(path);

        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile("file",
                file.getName(), "text/plain", IOUtils.toByteArray(input));

        Response response = given().param("file", multipartFile).when().post(ImdbController.USER_RATINGS_URL);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<Movie> result = response.getBody().as(List.class);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Time of the Wolf", result.get(0).getName());
        assertEquals("2003", result.get(0).getYear());

    }
}
