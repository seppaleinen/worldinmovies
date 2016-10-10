package se.david.backend.controllers.services;

import com.google.common.collect.Iterators;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import se.david.backend.controllers.repository.CountryRepository;
import se.david.backend.controllers.repository.MovieRepository;
import se.david.backend.controllers.services.util.CountriesImporter;
import se.david.backend.controllers.services.util.ImdbInterface;
import se.david.backend.controllers.services.util.ImdbProcessor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Level;

@Service
@Log
public class ImportService {
    @Autowired
    private ImdbInterface imdbInterface;
    @Autowired
    private ImdbProcessor imdbProcessor;
    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private CountriesImporter countriesImporter;

    public void importImdbCountries() {
        Resource result = imdbInterface.getResource();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(result.getInputStream()));

            Iterators.partition(reader.lines().skip(14).iterator(), 500).
                    forEachRemaining(batch -> movieRepository.save(imdbProcessor.process(batch)));
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void importCountries() {
        countryRepository.save(countriesImporter.importCountries());
    }
}
