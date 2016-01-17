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
import se.david.backend.controllers.repository.entities.CountryEntity;
import se.david.backend.controllers.repository.entities.MovieEntity;
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
    @Autowired
    private ImdbUserRatingsService imdbUserRatingsService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public void userRatings(@RequestParam File file) {
        imdbUserRatingsService.parseFromUserRatingsFile(file);
    }
}
