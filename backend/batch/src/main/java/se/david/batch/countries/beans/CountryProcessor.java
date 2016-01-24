package se.david.batch.countries.beans;

import lombok.extern.java.Log;
import org.springframework.batch.item.ItemProcessor;
import se.david.commons.Country;

@Log
public class CountryProcessor implements ItemProcessor<Country, Country> {
    @Override
    public Country process(Country countryEntity) throws Exception {
        //log.log(Level.INFO, "Processing: " + countryEntity);
        return countryEntity;
    }
}
