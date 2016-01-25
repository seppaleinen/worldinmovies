package se.david.batch.job.imdb.beans;

import lombok.extern.java.Log;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import se.david.batch.job.country.CountryRepository;
import se.david.commons.Country;
import se.david.commons.Movie;

import java.util.HashMap;
import java.util.Map;

@Log
public class CustomFieldSetMapper implements FieldSetMapper<Movie> {
    @Autowired
    private CountryRepository countryRepository;

    @Override
    public Movie mapFieldSet(FieldSet fieldSet) throws BindException {
        Movie entry = null;

        log.info("MAPFIELDSET: " + fieldSet.getFieldCount());
        if(fieldSet != null && fieldSet.getFieldCount() > 2) {
            entry = new Movie();

            entry.setName(fieldSet.readString(0));
            entry.setYear(fieldSet.readString(1));
            Country country = countryRepository.findByName(mapCountries(fieldSet.readRawString(3)));
            entry.setCountry(country);
        }

        return entry;
    }

    static String mapCountries(String imdbCountryName) {
        Map<String, String> specialCountries = new HashMap<>();
        specialCountries.put("Netherlands Antilles",    "Netherlands");
        specialCountries.put("Burma",                   "Myanmar");
        specialCountries.put("Ivory Coast",             "Côte d'Ivoire");
        specialCountries.put("Czechoslovakia",          "Czech Republic");
        specialCountries.put("Kosovo",                  "Serbia");
        specialCountries.put("Laos",                    "Lao People's Democratic Republic");
        specialCountries.put("Reunion",                 "Réunion");
        specialCountries.put("Siam",                    "Thailand");
        specialCountries.put("UK",                      "United Kingdom");
        specialCountries.put("USA",                     "United States");
        specialCountries.put("Soviet Union",            "Russian Federation");
        specialCountries.put("Vietnam",                 "Viet nam");
        specialCountries.put("Yugoslavia",              "Serbia");
        specialCountries.put("Zaire",                   "Congo, the Democratic Republic of the");

        String result = specialCountries.get(imdbCountryName);

        return result != null ? result : imdbCountryName;
    }
}