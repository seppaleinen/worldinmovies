package se.david.backend.controllers;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ResponseBody;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import se.david.backend.WorldInMoviesApplication;
import se.david.backend.controllers.repository.CountryRepository;
import se.david.backend.controllers.repository.MovieRepository;
import se.david.backend.controllers.repository.entities.Movie;
import se.david.backend.controllers.services.ImportService;
import se.david.backend.controllers.services.util.ImdbInterface;

import java.util.List;

import static com.jayway.restassured.RestAssured.when;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = WorldInMoviesApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "classpath:application-test.properties")
@TestPropertySource(locations="classpath:application-test.properties")
public class ImportControllerIT {
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private CountryRepository countryRepository;

    @LocalServerPort
    private int port;
    @Autowired
    private ImportService importService;
    @MockBean
    private ImdbInterface imdbInterface;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        RestAssured.port = port;
        movieRepository.deleteAll();
        countryRepository.deleteAll();
    }

    @Test
    public void testImportImdb() {
        Resource resource = new ClassPathResource("countries.small.list");
        Mockito.when(imdbInterface.getCountriesResource()).thenReturn(resource);

        assertEquals(0, movieRepository.count());

        Response result = when().post(ImportController.IMDB_COUNTRIES_URL);

        assertEquals(HttpStatus.CREATED.value(), result.getStatusCode());

        List<Movie> allMovies = movieRepository.findAll();
        boolean multipleCountries = false;
        for(Movie movie: allMovies) {
            if(movie.getCountrySet().size() > 1) {
                multipleCountries = true;
            }
        }
        assertTrue(multipleCountries);
        assertEquals(1691, movieRepository.count());
    }

    @Test
    public void testImportCountries() {
        assertEquals(0, countryRepository.count());

        Response result = when().post(ImportController.COUNTRIES_URL);

        assertEquals(HttpStatus.CREATED.value(), result.getStatusCode());

        assertEquals(249, countryRepository.count());
    }

    @Test
    public void testImportRatings() {
        Resource countriesResource = new ClassPathResource("countries.withrating.list");
        Mockito.when(imdbInterface.getCountriesResource()).thenReturn(countriesResource);

        assertEquals(0, movieRepository.count());

        Response countriesResult = when().post(ImportController.IMDB_COUNTRIES_URL);

        assertEquals(HttpStatus.CREATED.value(), countriesResult.getStatusCode());

        assertEquals("One movie should be created", 1, movieRepository.count());


        Resource ratingsResource = new ClassPathResource("ratings.withcountry.list");
        Mockito.when(imdbInterface.getRatingsResource()).thenReturn(ratingsResource);

        assertEquals("Movie should be updated, so still only one count", 1, movieRepository.count());

        Response ratingsResult = when().post(ImportController.IMDB_RATINGS_URL);

        assertEquals(HttpStatus.CREATED.value(), ratingsResult.getStatusCode());

        assertEquals("Movie should be updated, so still only one count", 1, movieRepository.count());
        Movie movie = movieRepository.findOne("The Shawshank Redemption:1994");
        assertNotNull(movie);
        assertEquals("Rating should be set", "9.3", movie.getRating());
    }
}
