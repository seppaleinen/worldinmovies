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

    @Bean
    public ItemReader<String> reader() {
        FlatFileItemReader<String> reader = new FlatFileItemReader<>();

        getInputStream();
        unzipFile();
        reader.setResource(new PathResource(System.getProperty("user.home") + "/" + "countries.list"));

        //reader.setResource(new ClassPathResource("countries.list"));
        reader.setLineMapper(new PassThroughLineMapper());
        reader.setLinesToSkip(14);
        reader.setEncoding(StandardCharsets.ISO_8859_1.name());

        return reader;
    }

    private void getInputStream() {
        final String ftpUrl = "ftp://ftp.sunet.se/pub/tv+movies/imdb/countries.list.gz";
        String userPath = System.getProperty("user.home");

        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        byte[] buffer = new byte[1024];

        try{
            long before = System.currentTimeMillis();
            log.info("Starging download of countries.list.gz");
            URL url = new URL(ftpUrl);
            URLConnection conn = url.openConnection();
            inputStream = conn.getInputStream();
            fileOutputStream = new FileOutputStream(userPath + "/" + "countries.list.gz");
            int bytes_read;
            while ((bytes_read = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, bytes_read);
            }

            long after = System.currentTimeMillis();
            log.info("Completed download of countries.list.gz in " + String.valueOf(after - before));

        }
        catch (IOException ex){
            ex.printStackTrace();
        } finally {
            try {
                if(inputStream != null) {
                    inputStream.close();
                }
                if(fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void unzipFile() {
        String userPath = System.getProperty("user.home");

        FileInputStream fileInputStream = null;
        GZIPInputStream gZIPInputStream = null;
        FileOutputStream fileOutputStream = null;
        byte[] buffer = new byte[1024];

        try {
            long before = System.currentTimeMillis();
            log.info("Unzipping file");
            fileInputStream = new FileInputStream(userPath + "/" + "countries.list.gz");
            gZIPInputStream = new GZIPInputStream(fileInputStream);
            fileOutputStream = new FileOutputStream(userPath + "/" + "countries.list");
            int bytes_read;
            while ((bytes_read = gZIPInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, bytes_read);
            }

            long after = System.currentTimeMillis();
            log.info("Unzipped file in " + String.valueOf(after - before));
            gZIPInputStream.close();
            fileOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(fileInputStream != null) {
                    fileInputStream.close();
                }
                if(gZIPInputStream != null) {
                    gZIPInputStream.close();
                }
                if(fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
                .<String, Movie> chunk(100)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
    // end::jobstep[]
}
