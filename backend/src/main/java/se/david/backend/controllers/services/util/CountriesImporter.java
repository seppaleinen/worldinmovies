package se.david.backend.controllers.services.util;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.extern.java.Log;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import se.david.backend.domain.Country;

import java.util.ArrayList;
import java.util.List;

@Component
@Log
public class CountriesImporter {
    private Resource resource = new ClassPathResource("countries.csv");

    /**
     * http://data.okfn.org/data/core/country-list/r/data.csv
     */
    public List<Country> importCountries(){
        List<Country> countryList = new ArrayList<>();
        try {
            CsvSchema schema = CsvSchema.builder()
                    .addColumn("name")
                    .addColumn("code")
                    .build();

            CsvMapper mapper = new CsvMapper();
            MappingIterator<Country> mappingIterator = mapper.readerFor(Country.class).with(schema).readValues(resource.getInputStream());
            countryList = mappingIterator.readAll();
        } catch (Exception e) {
            log.severe(e.getMessage());
        }

        for(Country country: countryList) {
            country.setId(country.getCode());
        }

        return countryList;
    }
}