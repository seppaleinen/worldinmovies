package se.david.batch.job.imdb;

import lombok.extern.java.Log;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import se.david.batch.job.imdb.beans.Helper;
import se.david.batch.job.imdb.beans.ImdbItemReader;
import se.david.batch.job.imdb.beans.ImdbItemWriter;
import se.david.batch.job.imdb.beans.ImdbProcessor;
import se.david.commons.Movie;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Service
@Log
public class MovieStep {
    @Autowired
    private ImdbProcessor imdbProcessor;
    @Autowired
    private ImdbItemWriter imdbItemWriter;
    @Autowired
    private Helper helper;
    @Autowired
    private ImdbItemReader imdbItemReader;

    private static final String ULTIMATE_REGEX = "\"?(.*?)\"?\\s+\\(([0-9?]{4})\\)?.*\\s([\\w \\.\\-\\(\\)]+)[\\s]*$";
    private static final Pattern ULTIMATE_PATTERN = Pattern.compile(ULTIMATE_REGEX);

    @Bean
    public ItemReader<String> reader() {
        FlatFileItemReader<String> reader = new FlatFileItemReader<>();

        reader.setResource(new ClassPathResource("countries.list"));
        LineMapper<String> lineMapper = new PassThroughLineMapper();
        reader.setLineMapper(lineMapper);
        reader.setLinesToSkip(15);
        reader.setEncoding(StandardCharsets.ISO_8859_1.name());

        return reader;
    }

    @Bean
    public ItemProcessor<String, Movie> processor() {
        return imdbProcessor;
    }

    @Bean
    public ItemWriter<Movie> writer(DataSource dataSource) {
        return imdbItemWriter;
    }
    // end::readerwriterprocessor[]

    @Bean(name = "movieSteps")
    public Step convertNewMovies(StepBuilderFactory stepBuilderFactory,
                                    ItemReader<String> reader,
                                    ItemWriter<Movie> writer,
                                    ItemProcessor<String, Movie> processor) {
        return stepBuilderFactory.get("convertNewMovies")
                .<String, Movie> chunk(10)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
    // end::jobstep[]
}
