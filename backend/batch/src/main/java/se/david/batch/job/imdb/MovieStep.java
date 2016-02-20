package se.david.batch.job.imdb;

import lombok.extern.java.Log;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.PathResource;
import org.springframework.stereotype.Service;
import se.david.batch.job.imdb.beans.ImdbItemReader;
import se.david.batch.job.imdb.beans.ImdbItemReaderHelper;
import se.david.batch.job.imdb.beans.ImdbItemWriter;
import se.david.batch.job.imdb.beans.ImdbProcessor;
import se.david.commons.Movie;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

@Service
@Log
public class MovieStep {
    @Autowired
    private ImdbProcessor imdbProcessor;
    @Autowired
    private ImdbItemWriter imdbItemWriter;
    @Autowired
    private ImdbItemReaderHelper imdbItemReaderHelper;

    @Bean
    public ItemReader<String> reader() {
        FlatFileItemReader<String> reader = new FlatFileItemReader<>();

        reader.setResource(imdbItemReaderHelper.getResource());
        reader.setLineMapper(new PassThroughLineMapper());
        reader.setLinesToSkip(14);
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

    @Bean(name = "movieSteps")
    public Step convertNewMovies(StepBuilderFactory stepBuilderFactory,
                                    ItemReader<String> reader,
                                    ItemWriter<Movie> writer,
                                    ItemProcessor<String, Movie> processor) {
        return stepBuilderFactory.get("convertNewMovies")
                .<String, Movie> chunk(100)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
}
