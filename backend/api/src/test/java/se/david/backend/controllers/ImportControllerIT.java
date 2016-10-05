package se.david.backend.controllers;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.ResponseBody;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import se.david.backend.WorldInMoviesApplication;
import se.david.backend.controllers.repository.MovieRepository;
import se.david.backend.controllers.services.ImportService;
import se.david.backend.controllers.services.util.ImdbInterface;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = WorldInMoviesApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class ImportControllerIT {
    @Autowired
    private MovieRepository movieRepository;
    @LocalServerPort
    private int port;
    @Autowired
    private ImportService importService;
    @Mock
    private ImdbInterface imdbInterface;

    @Before
    public void setup() {
        RestAssured.port = port;
        movieRepository.deleteAll();
        ReflectionTestUtils.setField(importService, "imdbInterface", imdbInterface);
    }

    @Test
    public void testImportImdb() {
        Resource resource = new ClassPathResource("countries.small.list");
        Mockito.when(imdbInterface.getResource()).thenReturn(resource);

        assertEquals(0, movieRepository.count());

        ResponseBody result = when().post(ImportController.IMDB_COUNTRIES_URL).getBody();

        assertEquals(1804, movieRepository.count());
    }
}
