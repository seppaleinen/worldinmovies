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
import se.david.backend.controllers.repository.MapRepository;
import se.david.backend.controllers.repository.entities.MapEntity;


import static com.jayway.restassured.RestAssured.given;
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
    private MapRepository repository;

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


        MapEntity mapEntity = new MapEntity();
        mapEntity.setParam("paramvalue");
        mapEntity.setId("idvalue");

        repository.save(mapEntity);

        Response response = given().param("id", "idvalue").when().get("/find");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(mapEntity.getParam(), response.getBody().print());
    }
}
