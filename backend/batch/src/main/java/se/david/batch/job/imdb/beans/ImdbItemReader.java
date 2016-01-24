package se.david.batch.job.imdb.beans;

import lombok.extern.java.Log;
import org.springframework.batch.item.*;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.RegexLineTokenizer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import se.david.commons.Movie;

import java.util.regex.Pattern;

@Service
@Log
public class ImdbItemReader implements ItemReader<Movie> {
    private static final String regexNameAndYear = "\"?(.*?)\"?\\s+\\(([0-9?]{4})\\)?";
    private static final String regexCountry = "\\t([\\w \\.\\-\\(\\)]+)[\\s]*$";

    private static final Pattern patternNameAndYear = Pattern.compile(regexNameAndYear);
    private static final Pattern patternCountry = Pattern.compile(regexCountry);

    @Override
    public Movie read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        FlatFileItemReader<Movie> reader = new FlatFileItemReader<>();

        RegexLineTokenizer regexLineTokenizer = new RegexLineTokenizer();
        regexLineTokenizer.setPattern(patternNameAndYear);

        reader.setResource(new ClassPathResource("countries.csv"));

        DefaultLineMapper<Movie> defaultLineMapper = new DefaultLineMapper<>();
        defaultLineMapper.setLineTokenizer(regexLineTokenizer);
        defaultLineMapper.setFieldSetMapper(new CustomFieldSetMapper());
        defaultLineMapper.afterPropertiesSet();

        reader.setLineMapper(defaultLineMapper);

        return reader.read();
    }
}
