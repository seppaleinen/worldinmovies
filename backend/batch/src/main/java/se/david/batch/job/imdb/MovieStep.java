package se.david.batch.job.imdb;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.RegexLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import se.david.batch.job.imdb.beans.CustomFieldSetMapper;
import se.david.batch.job.imdb.beans.Helper;
import se.david.batch.job.imdb.beans.ImdbItemReader;
import se.david.batch.job.imdb.beans.ImdbItemWriter;
import se.david.batch.job.imdb.beans.ImdbProcessor;
import se.david.commons.Movie;

import javax.sql.DataSource;
import java.util.regex.Pattern;

@Service
public class MovieStep {
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

        reader.setResource(new ClassPathResource("job.list"));

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

    @Bean(name = "movieSteps")
    public Step convertNewMovies(StepBuilderFactory stepBuilderFactory,
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
