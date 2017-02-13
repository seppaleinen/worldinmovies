package se.david.backend.controllers;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import se.david.backend.WorldInMoviesApplication;
import se.david.backend.repository.CountryRepository;
import se.david.backend.repository.MovieRepository;
import se.david.backend.domain.Movie;
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
        //multipleCountries = allMovies.stream().anyMatch(movie -> movie.getCountrySet().size() > 1);
        assertEquals(1680, movieRepository.count());
        assertTrue("No movie with multiple countries", multipleCountries);
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

        assertEquals("Two movies should be created", 2, movieRepository.count());


        Resource ratingsResource = new ClassPathResource("ratings.withcountry.list");
        Mockito.when(imdbInterface.getRatingsResource()).thenReturn(ratingsResource);

        assertEquals("Movie should be updated, so still only two counts", 2, movieRepository.count());

        Response ratingsResult = when().post(ImportController.IMDB_RATINGS_URL);

        assertEquals(HttpStatus.CREATED.value(), ratingsResult.getStatusCode());

        assertEquals("Movie should be updated, so still only two counts", 2, movieRepository.count());
        Movie movie = movieRepository.findOne("The Shawshank Redemption:1994");
        assertNotNull(movie);
        assertEquals("Rating should be set", "9.3", movie.getRating());
        assertNotNull("WeightedRating should be set", movie.getWeightedRating());

        Movie movie2 = movieRepository.findOne("9 Days:2013");
        assertNotNull(movie2);
        assertEquals("Rating should be set", "3.7", movie2.getRating());
        assertNotNull("WeightedRating should be set", movie2.getWeightedRating());
    }
}
