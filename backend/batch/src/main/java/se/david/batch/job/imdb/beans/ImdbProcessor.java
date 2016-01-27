package se.david.batch.job.imdb.beans;

import lombok.extern.java.Log;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.david.batch.job.country.CountryRepository;
import se.david.commons.Movie;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Log
public class ImdbProcessor implements ItemProcessor<String, Movie> {
    private static final String ULTIMATE_REGEX = "\"?(.*?)\"?\\s+\\(([0-9?]{4})\\)?.*\\s([\\w \\.\\-\\(\\)]+)[\\s]*$";
    private static final Pattern ULTIMATE_PATTERN = Pattern.compile(ULTIMATE_REGEX);

    private static final Map<String, String> specialCountries = new HashMap<>();

    static {
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
    }

    @Autowired
    private CountryRepository countryRepository;

    @Override
    public Movie process(String string) throws Exception {
        Movie movie = null;

        Matcher matcher = ULTIMATE_PATTERN.matcher(string);
        if(matcher.matches()) {
            log.log(Level.FINE, "Matched: " + string);
            movie = new Movie();
            movie.setName(matcher.group(1));
            movie.setYear(matcher.group(2));
            /**
            String country = specialCountries.get(matcher.group(3));
            if(country == null) {
                movie.setCountry(countryRepository.findByName(matcher.group(3)));
            } else {
                movie.setCountry(countryRepository.findByName(country));
            }
             **/
            movie.setId(movie.getName() + ":" + movie.getYear());
        } else {
            log.log(Level.INFO, "No Matched: " + string);
        }

        return movie;
    }
}

