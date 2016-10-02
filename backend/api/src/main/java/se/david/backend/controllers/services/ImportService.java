package se.david.backend.controllers.services;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import se.david.backend.controllers.repository.MovieRepository;
import se.david.backend.controllers.services.util.ImdbInterface;
import se.david.backend.controllers.services.util.ImdbProcessor;
import se.david.commons.Movie;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Log
public class ImportService {
    @Autowired
    private ImdbInterface imdbInterface;
    @Autowired
    private ImdbProcessor imdbProcessor;
    @Autowired
    private MovieRepository movieRepository;

    public void importImdbCountries() {
        Resource result = imdbInterface.getResource();
        try (Stream<String> stream = Files.lines(Paths.get(result.getFile().getPath()), StandardCharsets.ISO_8859_1)) {
            for(String row: stream.skip(14).collect(Collectors.toList())) {
                Movie movie = imdbProcessor.process(row);
                movieRepository.save(movie);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage());
        }


    }

}
