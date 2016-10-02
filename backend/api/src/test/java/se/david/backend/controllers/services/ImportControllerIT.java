package se.david.backend.controllers.services;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ResponseBody;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.multipart.MultipartFile;
import se.david.backend.WorldInMoviesApplication;
import se.david.backend.controllers.ImdbController;
import se.david.backend.controllers.ImportController;
import se.david.backend.controllers.repository.MovieRepository;
import se.david.commons.Movie;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = WorldInMoviesApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class ImportControllerIT {
    @Autowired
    private MovieRepository movieRepository;
    @Value("${local.server.port}")
    private int port;

    @Before
    public void setup() {
        RestAssured.port = port;
    }

    @Test
    public void test() {
        ResponseBody result = when().post(ImportController.IMDB_COUNTRIES).getBody();
    }
}
