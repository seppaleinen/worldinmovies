package se.david.backend.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import se.david.backend.domain.Country;

import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;

import static org.junit.Assert.*;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
public class CountryRepositoryIT {
    @Autowired
    private CountryRepository countryRepository;

    @Before
    public void setup() {
        countryRepository.deleteAll();
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
