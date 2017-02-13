package se.david.backend.repository;

import com.jayway.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import se.david.backend.WorldInMoviesApplication;
import se.david.backend.domain.Country;

import java.util.Collections;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = WorldInMoviesApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "classpath:application-test.properties")
@TestPropertySource(locations="classpath:application-test.properties")
public class CountryRepositoryIT {
    @Autowired
    private CountryRepository countryRepository;

    @LocalServerPort
    private int port;

    @Before
    public void setup() {
        countryRepository.deleteAll();
        RestAssured.port = port;
    }

    @Test
    public void ExpectFindByNameToResultInSavedEntity() {
        Country country = Country.builder().
                id("SE").
                code("SE").
                name("Sweden").
                build();

        countryRepository.save(Collections.singletonList(country));

        Country result = countryRepository.findByName("Sweden");

        assertNotNull("Result should not be null", result);
        assertEquals(country.getId(), result.getId());

    }
}
