package se.david.batch.job.imdb.beans;

import lombok.extern.java.Log;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

@Service
@Log
public class ImdbItemReaderHelper {
    public Resource getResource() {
        getInputStream();
        unzipFile();

        return new PathResource(System.getProperty("user.home") + "/" + "countries.list");
    }

    private void getInputStream() {
        final String ftpUrl = "ftp://ftp.funet.fi/pub/mirrors/ftp.imdb.com/pub/countries.list.gz";
        String userPath = System.getProperty("user.home");

        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        byte[] buffer = new byte[1024];

        try{
            long before = System.currentTimeMillis();
            log.info("Starting download of countries.list.gz");
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
