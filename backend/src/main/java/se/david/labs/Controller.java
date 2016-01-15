package se.david.labs;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST})
@Log
public class Controller {
    @Autowired
    private KrogRouletteRepository krogRouletteRepository;

    @RequestMapping(value = "/save", method = RequestMethod.GET)
    public String save(@RequestParam String param) {
        log.fine("yeah: ");
        KrogRouletteEntity krogRouletteEntity = new KrogRouletteEntity();
        krogRouletteEntity.setParam(param);
        KrogRouletteEntity savedEntity = krogRouletteRepository.save(krogRouletteEntity);
        return savedEntity.getId();
    }

    @RequestMapping(value = "find", method = RequestMethod.GET)
    public String find(@RequestParam String id) {
        log.fine("Find");
        KrogRouletteEntity result = krogRouletteRepository.findOne(id);
        return result.getParam();
    }
}
