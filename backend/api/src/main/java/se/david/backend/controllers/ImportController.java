package se.david.backend.controllers;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.david.backend.controllers.services.ImportService;

@RestController
@CrossOrigin(origins = "*", methods = {RequestMethod.POST})
@Log
public class ImportController {
    private static final String ROOT_URL = "/import";
    public static final String IMDB_COUNTRIES_URL = ROOT_URL + "/startImdbImport";
    public static final String COUNTRIES_URL = ROOT_URL + "/startCountriesImport";
    @Autowired
    private ImportService importService;

    @RequestMapping(value = IMDB_COUNTRIES_URL, method = RequestMethod.POST)
    public ResponseEntity importImdbCountryList() {
        importService.importImdbCountries();
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @RequestMapping(value = COUNTRIES_URL, method = RequestMethod.POST)
    public ResponseEntity importCountries() {
        importService.importCountries();
        return new ResponseEntity(HttpStatus.CREATED);
    }
}
