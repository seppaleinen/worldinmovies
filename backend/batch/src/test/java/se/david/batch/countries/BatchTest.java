package se.david.batch.countries;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.david.batch.WorldInMoviesBatchApplication;
import se.david.commons.Country;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {WorldInMoviesBatchApplication.class})
// NOTE!! order is important
@IntegrationTest("server.port:0")
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class BatchTest {
    @Autowired
    private CountryRepository repository;

    @Before
    public void setup() {
    }

    @Test
    public void canFindMapByGet() {
        List<Country> result = repository.findAll();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}
