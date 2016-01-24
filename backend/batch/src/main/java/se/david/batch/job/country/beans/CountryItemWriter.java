package se.david.batch.job.country.beans;

import lombok.extern.java.Log;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.david.batch.job.country.CountryRepository;
import se.david.commons.Country;

import java.util.List;

@Service
@Log
public class CountryItemWriter implements ItemWriter<Country> {
    @Autowired
    private CountryRepository countryRepository;

    @Override
    public void write(List<? extends Country> list) throws Exception {
        for(Country countryEntity : list) {
            if(countryRepository.findByCode(countryEntity.getCode()) == null) {
                log.info("Saving new country: " + countryEntity.getName());
                countryRepository.save(countryEntity);
            }
        }
    }
}
