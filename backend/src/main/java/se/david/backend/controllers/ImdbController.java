package se.david.backend.controllers;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import se.david.backend.domain.Movie;
import se.david.backend.controllers.services.ImdbService;

import java.util.List;

@RestController
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST})
@Log
public class ImdbController {
    private static final String ROOT_URL = "/imdb";
    public static final String USER_RATINGS_URL = ROOT_URL + "/userRatings";
    public static final String MOVIES_BY_COUNTRY = ROOT_URL + "/movies/country";
    @Autowired
    private ImdbService imdbService;

    @RequestMapping(value = USER_RATINGS_URL, method = RequestMethod.POST)
    public ResponseEntity<List<Movie>> userRatings(@RequestParam("file") MultipartFile file, @RequestParam("username") String username) {
        return new ResponseEntity<>(imdbService.parseFromUserRatingsFile(file, username), HttpStatus.OK);
    }

    @RequestMapping(value = MOVIES_BY_COUNTRY, method = RequestMethod.GET)
    public ResponseEntity<List<Movie>> moviesByCountry(@RequestParam("country") String country) {
        return new ResponseEntity<>(imdbService.getMoviesByCountry(country), HttpStatus.OK);
    }
}
