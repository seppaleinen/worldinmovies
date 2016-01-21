package se.david.backend.controllers;

import lombok.extern.java.Log;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import se.david.backend.controllers.repository.MovieRepository;
import se.david.backend.controllers.repository.entities.CountryEntity;
import se.david.backend.controllers.repository.entities.MovieEntity;
import se.david.backend.controllers.services.ImdbMovieListService;
import se.david.backend.controllers.services.ImdbUserRatingsService;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST})
@Log
public class ImdbController {
    private static final String ROOT_URL = "/imdb";
    public static final String USER_RATINGS_URL = ROOT_URL + "/userRatings";
    public static final String INIT_URL = ROOT_URL + "/init";
    public static final String FIND_MOVIES_URL = ROOT_URL + "/findMovies";
    @Autowired
    private ImdbUserRatingsService imdbUserRatingsService;
    @Autowired
    private ImdbMovieListService imdbMovieListService;
    @Autowired
    private MovieRepository movieRepository;

    // @TODO http://stackoverflow.com/questions/25699727/multipart-file-upload-spring-boot
    @RequestMapping(value = USER_RATINGS_URL, method = RequestMethod.POST)
    public void userRatings(@RequestParam File file) {
        imdbUserRatingsService.parseFromUserRatingsFile(file);
    }

    @Deprecated
    @RequestMapping(value = INIT_URL, method = RequestMethod.POST)
    public void init() throws Exception {
        imdbMovieListService.init();
    }

    @RequestMapping(value = FIND_MOVIES_URL, method = RequestMethod.GET)
    public List<MovieEntity> findMovies() throws Exception {
        return movieRepository.findAll();
    }

}
