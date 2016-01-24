package se.david.backend.controllers;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import se.david.backend.WorldInMoviesApplication;
import se.david.backend.controllers.repository.CountryRepository;
import se.david.commons.Country;


import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {WorldInMoviesApplication.class})
// NOTE!! order is important
@WebAppConfiguration
@IntegrationTest("server.port:0")
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class MapControllerIT {
    @Autowired
    private CountryRepository repository;

    @Value("${local.server.port}")
    private int port;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        //ReflectionTestUtils.setField(callbackService, "restTemplate", restTemplateMock);
        repository.deleteAll();
        RestAssured.port = port;
    }

    @Test
    public void canFindMapByGet() {
        given().param(MapController.FIND_URL, "asd").
                when().post(MapController.FIND_URL).
                then().statusCode(HttpStatus.METHOD_NOT_ALLOWED.value());
        given().param(MapController.FIND_URL, "asd").
                when().delete(MapController.FIND_URL).
                then().statusCode(HttpStatus.METHOD_NOT_ALLOWED.value());
        given().param(MapController.FIND_URL, "asd").
                when().put(MapController.FIND_URL).
                then().statusCode(HttpStatus.METHOD_NOT_ALLOWED.value());
        given().param(MapController.FIND_URL, "asd").
                when().patch(MapController.FIND_URL).
                then().statusCode(HttpStatus.METHOD_NOT_ALLOWED.value());


        Country countryEntity = new Country();
        countryEntity.setCode("paramvalue");
        countryEntity.setId("idvalue");

        repository.save(countryEntity);

        Response response = given().param("id", "idvalue").when().get(MapController.FIND_URL);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(countryEntity.getCode(), response.getBody().print());
    }

    @Test
    public void canFindAllCountries() {
        when().post(MapController.FIND_COUNTRIES).
                then().statusCode(HttpStatus.METHOD_NOT_ALLOWED.value());
        when().delete(MapController.FIND_COUNTRIES).
                then().statusCode(HttpStatus.METHOD_NOT_ALLOWED.value());
        when().put(MapController.FIND_COUNTRIES).
                then().statusCode(HttpStatus.METHOD_NOT_ALLOWED.value());
        when().patch(MapController.FIND_COUNTRIES).
                then().statusCode(HttpStatus.METHOD_NOT_ALLOWED.value());


        Country countryEntity = new Country();
        countryEntity.setCode("paramvalue");
        countryEntity.setId("idvalue");

        repository.save(countryEntity);

        Response response = when().get(MapController.FIND_COUNTRIES);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        List<Country> result = response.getBody().as(List.class);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

}
