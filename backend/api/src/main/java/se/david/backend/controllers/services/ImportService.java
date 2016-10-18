package se.david.backend.controllers.services;

import com.google.common.collect.Iterators;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import se.david.backend.controllers.repository.CountryRepository;
import se.david.backend.controllers.repository.MovieRepository;
import se.david.backend.controllers.services.util.CountriesImporter;
import se.david.backend.controllers.services.util.ImdbCountryProcessor;
import se.david.backend.controllers.services.util.ImdbInterface;
import se.david.backend.controllers.services.util.ImdbRatingsProcessor;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.stream.Stream;

@Service
@Log
public class ImportService {
    @Autowired
    private ImdbInterface imdbInterface;
    @Autowired
    private ImdbCountryProcessor imdbCountryProcessor;
    @Autowired
    private ImdbRatingsProcessor imdbRatingsProcessor;
    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private CountriesImporter countriesImporter;

    public void importImdbCountries() {
        Resource result = imdbInterface.getCountriesResource();
        try (Stream<String> stream = Files.lines(Paths.get(result.getFile().getPath()), StandardCharsets.ISO_8859_1)) {
            Iterators.partition(stream.skip(14).iterator(), 500).
                    forEachRemaining(batch -> movieRepository.save(imdbCountryProcessor.process(batch)));
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void importImdbRatings() {
        Resource result = imdbInterface.getRatingsResource();
        try (Stream<String> stream = Files.lines(Paths.get(result.getFile().getPath()), StandardCharsets.ISO_8859_1)) {
            Iterators.partition(stream.skip(296).iterator(), 500).forEachRemaining(batch -> movieRepository.save(imdbRatingsProcessor.process(batch)));
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void importCountries() {
        countryRepository.save(countriesImporter.importCountries());
    }
}
