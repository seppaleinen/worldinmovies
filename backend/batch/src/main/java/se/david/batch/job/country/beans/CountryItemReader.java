package se.david.batch.job.country.beans;

import lombok.extern.java.Log;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import se.david.commons.Country;

@Service
@Log
public class CountryItemReader implements ItemReader<Country>, ItemStream {
    private FlatFileItemReader<Country> reader = new FlatFileItemReader<>();

    @Override
    public Country read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
         // http://data.okfn.org/data/core/country-list/r/data.csv
        reader.setResource(new ClassPathResource("countries.csv"));
        reader.setLineMapper(new DefaultLineMapper<Country>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames(new String[] { "name", "code" });
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<Country>() {{
                setTargetType(Country.class);
            }});
        }});


        return reader.read();
    }

    @Override
    public void close() throws ItemStreamException {
        reader.close();
    }

    @Override
    public void open(ExecutionContext arg0) throws ItemStreamException {
        reader.open(arg0);
    }

    @Override
    public void update(ExecutionContext arg0) throws ItemStreamException {
        reader.update(arg0);
    }
}
