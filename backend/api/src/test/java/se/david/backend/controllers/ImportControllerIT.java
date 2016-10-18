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
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import se.david.backend.WorldInMoviesApplication;
import se.david.backend.controllers.repository.CountryRepository;
import se.david.backend.controllers.repository.MovieRepository;
import se.david.backend.controllers.services.ImportService;
import se.david.backend.controllers.services.util.ImdbInterface;

import static com.jayway.restassured.RestAssured.when;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = WorldInMoviesApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "classpath:application-test.properties")
@ContextConfiguration(classes = WorldInMoviesApplication.class)
public class ImportControllerIT {
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private CountryRepository countryRepository;

    @LocalServerPort
    private int port;
    @Autowired
    private ImportService importService;
    @Mock
    private ImdbInterface imdbInterface;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        RestAssured.port = port;
        movieRepository.deleteAll();
        countryRepository.deleteAll();
        ReflectionTestUtils.setField(importService, "imdbInterface", imdbInterface);
    }

    @Test
    public void testImportImdb() {
        Resource resource = new ClassPathResource("countries.small.list");
        Mockito.when(imdbInterface.getCountriesResource()).thenReturn(resource);

        assertEquals(0, movieRepository.count());

        Response result = when().post(ImportController.IMDB_COUNTRIES_URL);

        assertEquals(HttpStatus.CREATED.value(), result.getStatusCode());

        assertEquals(1804, movieRepository.count());
    }

    @Test
    public void testImportCountries() {
        assertEquals(0, countryRepository.count());

        Response result = when().post(ImportController.COUNTRIES_URL);

        assertEquals(HttpStatus.CREATED.value(), result.getStatusCode());

        assertEquals(249, countryRepository.count());
    }
}
