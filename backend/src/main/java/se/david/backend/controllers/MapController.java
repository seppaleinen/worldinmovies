package se.david.backend.controllers;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import se.david.backend.controllers.repository.MapRepository;
import se.david.backend.controllers.repository.entities.MapEntity;

@RestController
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST})
@Log
public class MapController {
    public static final String SAVE_URL = "/save";
    public static final String FIND_URL = "/find";
    @Autowired
    private MapRepository mapRepository;

    @RequestMapping(value = "/save", method = RequestMethod.GET)
    public String save(@RequestParam String param) {
        log.fine("yeah: ");
        MapEntity mapEntity = new MapEntity();
        mapEntity.setParam(param);
        MapEntity savedEntity = mapRepository.save(mapEntity);
        return savedEntity.getId();
    }

    @RequestMapping(value = "find", method = RequestMethod.GET)
    public String find(@RequestParam String id) {
        log.fine("Find");
        MapEntity result = mapRepository.findOne(id);
        return result.getParam();
    }
}
