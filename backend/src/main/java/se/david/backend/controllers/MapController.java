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
import se.david.backend.repository.CountryRepository;
import se.david.backend.domain.Country;

import java.util.List;

@RestController
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST})
@Log
public class MapController {
    private static final String ROOT_URL = "/map";
    public static final String FIND_URL = ROOT_URL + "/find";
    public static final String FIND_COUNTRIES = ROOT_URL + "/findCountries";
    @Autowired
    private CountryRepository countryRepository;

    @RequestMapping(value = FIND_URL, method = RequestMethod.GET)
    public ResponseEntity<String> find(@RequestParam String id) {
        Country result = countryRepository.findOne(id);
        return new ResponseEntity<>(result.getCode(), HttpStatus.OK);
    }

    @RequestMapping(value = FIND_COUNTRIES, method = RequestMethod.GET)
    public ResponseEntity<List<Country>> findAllCountries() {
        return new ResponseEntity<>(countryRepository.findAll(), HttpStatus.OK);
    }
}
