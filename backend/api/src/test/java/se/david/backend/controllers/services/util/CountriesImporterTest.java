package se.david.backend.controllers.services.util;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import se.david.backend.controllers.repository.entities.Country;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CountriesImporterTest {
    @InjectMocks
    private CountriesImporter countriesImporter;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test() {
        List<Country> list = countriesImporter.importCountries();

        assertNotNull(list);
        assertEquals(249, list.size());

        for(Country country: list) {
            assertNotNull(country);
            assertNotNull("Id should not be null: " + country.toString(), country.getId());
            assertNotNull("Name should not be null: " + country.toString(), country.getName());
            assertNotNull("Code should not be null: " + country.toString(), country.getCode());
        }
    }
}
