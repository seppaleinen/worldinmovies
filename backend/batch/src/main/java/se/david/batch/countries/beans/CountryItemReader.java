package se.david.batch.countries.beans;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import se.david.commons.Country;

public class CountryItemReader implements ItemReader<Country> {
    @Override
    public Country read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        return null;
    }
}
