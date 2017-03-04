package se.david.backend.controllers;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import se.david.backend.WorldInMoviesApplication;
import se.david.backend.repository.CountryRepository;
import se.david.backend.domain.Country;

import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = WorldInMoviesApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class MapControllerIT {
    @Autowired
    private CountryRepository repository;
    @LocalServerPort
    private int port;

    @Before
    public void setup() {
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

        List result = response.getBody().as(List.class);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

}
