package se.david.backend.controllers;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import se.david.backend.controllers.repository.entities.Movie;
import se.david.backend.controllers.services.ImdbService;

import java.util.List;

@RestController
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST})
@Log
public class ExportController {
    private static final String ROOT_URL = "/export";
    public static final String EXPORT_IMDB_CSV = ROOT_URL + "/imdb";
    @Autowired
    private ImdbService imdbService;

    @RequestMapping(value = EXPORT_IMDB_CSV, method = RequestMethod.POST)
    public List<Movie> userRatings(@RequestParam("file") MultipartFile file) {
        return imdbService.parseFromUserRatingsFile(file);
    }

}
