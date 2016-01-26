package se.david.batch.job.imdb.beans;

import lombok.extern.java.Log;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.david.batch.job.country.CountryRepository;
import se.david.commons.Movie;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Log
public class ImdbProcessor implements ItemProcessor<String, Movie> {
    private static final String ULTIMATE_REGEX = "\"?(.*?)\"?\\s+\\(([0-9?]{4})\\)?.*\\s([\\w \\.\\-\\(\\)]+)[\\s]*$";
    private static final Pattern ULTIMATE_PATTERN = Pattern.compile(ULTIMATE_REGEX);

    @Autowired
    private CountryRepository countryRepository;

    @Override
    public Movie process(String string) throws Exception {
        Movie movie = null;

        Matcher matcher = ULTIMATE_PATTERN.matcher(string);
        if(matcher.matches()) {
            log.fine("Matched: " + string);
            movie = new Movie();
            movie.setName(matcher.group(1));
            movie.setYear(matcher.group(2));
            String country = mapCountries(matcher.group(3));
            movie.setCountry(countryRepository.findByName(country));
        } else {
            log.fine("No Matched: " + string);
        }
        //log.log(Level.INFO, "Processing: " + countryEntity);
        return movie;
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

