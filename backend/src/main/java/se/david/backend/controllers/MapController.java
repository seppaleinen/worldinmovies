package se.david.backend.controllers;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import se.david.backend.controllers.repository.CountryRepository;
import se.david.backend.controllers.repository.entities.CountryEntity;

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
    public String find(@RequestParam String id) {
        log.fine("Find");
        CountryEntity result = countryRepository.findOne(id);
        return result.getCode();
    }

    @RequestMapping(value = FIND_COUNTRIES, method = RequestMethod.GET)
    public List<CountryEntity> findAllCountries() {
        return countryRepository.findAll();
    }
}
