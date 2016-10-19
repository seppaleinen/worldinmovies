package se.david.backend.controllers.services.util;

import lombok.extern.java.Log;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

@Component
@Log
public class ImdbInterface {
    private static final String ftpUrl = "ftp://ftp.funet.fi/pub/mirrors/ftp.imdb.com/pub/";
    private static final String countriesFile = "countries.list.gz";
    private static final String ratingsFile = "ratings.list.gz";

    public Resource getCountriesResource() {
        getInputStream(countriesFile);
        unzipFile(countriesFile);

        return new PathResource(System.getProperty("user.home") + "/" + "countries.list");
    }

    public Resource getRatingsResource() {
        getInputStream(ratingsFile);
        unzipFile(ratingsFile);

        return new PathResource(System.getProperty("user.home") + "/" + "ratings.list");
    }


    private void getInputStream(String filename) {
        String userPath = System.getProperty("user.home");

        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        byte[] buffer = new byte[1024];

        try{
            long before = System.currentTimeMillis();
            log.info("Starting download of " + filename);
            URL url = new URL(ftpUrl + filename);
            URLConnection conn = url.openConnection();
            inputStream = conn.getInputStream();
            fileOutputStream = new FileOutputStream(userPath + "/" + filename);
            int bytes_read;
            while ((bytes_read = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, bytes_read);
            }

            long after = System.currentTimeMillis();
            log.info("Completed download of " + filename + " in " + String.valueOf(after - before));

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

    private void unzipFile(String filename) {
        String userPath = System.getProperty("user.home");

        FileInputStream fileInputStream = null;
        GZIPInputStream gZIPInputStream = null;
        FileOutputStream fileOutputStream = null;
        byte[] buffer = new byte[1024];

        try {
            long before = System.currentTimeMillis();
            log.info("Unzipping file");
            fileInputStream = new FileInputStream(userPath + "/" + filename);
            gZIPInputStream = new GZIPInputStream(fileInputStream);
            fileOutputStream = new FileOutputStream(userPath + "/" + filename.split(".gz")[0]);
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
