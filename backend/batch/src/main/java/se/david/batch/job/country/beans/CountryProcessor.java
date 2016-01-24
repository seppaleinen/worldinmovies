package se.david.batch.job.country.beans;

import lombok.extern.java.Log;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Service;
import se.david.commons.Country;

@Service
@Log
public class CountryProcessor implements ItemProcessor<Country, Country> {
    @Override
    public Country process(Country countryEntity) throws Exception {
        //log.log(Level.INFO, "Processing: " + countryEntity);
        return countryEntity;
    }
}
