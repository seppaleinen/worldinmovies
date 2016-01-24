package se.david.batch.imdb;

import lombok.extern.java.Log;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.RegexLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import se.david.batch.countries.beans.CountryItemReader;
import se.david.batch.countries.beans.CountryItemWriter;
import se.david.batch.countries.beans.CountryProcessor;
import se.david.batch.imdb.beans.*;
import se.david.commons.Country;
import se.david.commons.Movie;

import javax.sql.DataSource;
import java.io.File;
import java.net.URL;
import java.util.regex.Pattern;

//@Configuration
//@EnableBatchProcessing
@Log
public class ImdbBatchConfiguration {
    @Autowired
    private ImdbProcessor imdbProcessor;
    @Autowired
    private ImdbItemWriter imdbItemWriter;
    @Autowired
    private Helper helper;
    @Autowired
    private ImdbItemReader imdbItemReader;

    private static final String regexNameAndYear = "\"?(.*?)\"?\\s+\\(([0-9?]{4})\\)?";
    private static final String regexCountry = "\\t([\\w \\.\\-\\(\\)]+)[\\s]*$";

    private static final Pattern patternNameAndYear = Pattern.compile(regexNameAndYear);
    private static final Pattern patternCountry = Pattern.compile(regexCountry);

    @Bean
    public ItemReader<Movie> reader() {
        FlatFileItemReader<Movie> reader = new FlatFileItemReader<>();

        RegexLineTokenizer regexLineTokenizer = new RegexLineTokenizer();
        regexLineTokenizer.setPattern(patternNameAndYear);

        reader.setResource(new ClassPathResource("countries.list"));

        DefaultLineMapper<Movie> defaultLineMapper = new DefaultLineMapper<>();
        defaultLineMapper.setLineTokenizer(regexLineTokenizer);
        defaultLineMapper.setFieldSetMapper(new CustomFieldSetMapper());
        defaultLineMapper.afterPropertiesSet();

        reader.setLineMapper(defaultLineMapper);

        return reader;
    }

    @Bean
    public ItemProcessor<Movie, Movie> processor() {
        return imdbProcessor;
    }

    @Bean
    public ItemWriter<Movie> writer(DataSource dataSource) {
        return imdbItemWriter;
    }
    // end::readerwriterprocessor[]

    // tag::jobstep[]
    @Bean
    public Job importNewCountries(JobBuilderFactory jobs, Step s1, @Qualifier(value = "ImdbBatchListener") JobExecutionListener listener) {
        return jobs.get("ImportNewMovies")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(s1)
                .end()
                .build();
    }

    @Bean
    public Step convertNewCountries(StepBuilderFactory stepBuilderFactory,
                                    ItemReader<Movie> reader,
                                    ItemWriter<Movie> writer,
                                    ItemProcessor<Movie, Movie> processor) {
        return stepBuilderFactory.get("convertNewMovies")
                .<Movie, Movie> chunk(10)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
    // end::jobstep[]
}
