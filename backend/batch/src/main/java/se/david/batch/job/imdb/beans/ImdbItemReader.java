package se.david.batch.job.imdb.beans;

import lombok.extern.java.Log;
import org.springframework.batch.item.*;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.batch.item.file.transform.RegexLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import se.david.commons.Movie;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

//@Service
@Log
public class ImdbItemReader implements ItemReader<String> {
    private static final String regexNameAndYear = "\"?(.*?)\"?\\s+\\(([0-9?]{4})\\)?";
    private static final String regexCountry = "\\t([\\w \\.\\-\\(\\)]+)[\\s]*$";

    private static final Pattern patternNameAndYear = Pattern.compile(regexNameAndYear);
    private static final Pattern patternCountry = Pattern.compile(regexCountry);

    FlatFileItemReader<String> reader = new FlatFileItemReader<>();

    //@Bean
    public String read() throws Exception {
        //getInputStream();
        //unzipFile();
        //reader.setResource(new PathResource(System.getProperty("user.home") + "/" + "countries.list"));

        reader.setResource(new ClassPathResource("countries.list"));
        reader.setLineMapper(new PassThroughLineMapper());
        reader.setLinesToSkip(14);
        reader.setEncoding(StandardCharsets.ISO_8859_1.name());

        return reader.read();
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
}
