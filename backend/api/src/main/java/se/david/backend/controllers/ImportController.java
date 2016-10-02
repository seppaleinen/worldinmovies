package se.david.backend.controllers;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import se.david.backend.controllers.services.ImdbService;
import se.david.backend.controllers.services.ImportService;
import se.david.commons.Movie;

import java.util.List;

@RestController
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST})
@Log
public class ImportController {
    private static final String ROOT_URL = "/import";
    public static final String IMDB_COUNTRIES = ROOT_URL + "/importImdbCountries";
    @Autowired
    private ImportService importService;

    @RequestMapping(value = IMDB_COUNTRIES, method = RequestMethod.POST)
    public HttpEntity importImdbCountyList() {
        try {
            importService.importImdbCountries();
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
