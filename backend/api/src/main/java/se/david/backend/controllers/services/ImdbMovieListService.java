package se.david.backend.controllers.services;

import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.david.backend.controllers.repository.CountryRepository;
import se.david.backend.controllers.repository.MovieRepository;
import se.david.commons.Movie;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

/**
 * 1. Download countries.list.gz (ftp://ftp.sunet.se/pub/tv+movies/imdb/countries.list.gz)
 * 2. Unzip
 * 3. Parse all movies to MovieEntity
 * 4. Persist list of movies
 */
@Service
public class ImdbMovieListService {
    private static final String regexNameAndYear = "\"?(.*?)\"?\\s+\\(([0-9?]{4})\\)?";
    private static final String regexCountry = "\\t([\\w \\.\\-\\(\\)]+)[\\s]*$";

    private static final Pattern patternNameAndYear = Pattern.compile(regexNameAndYear);
    private static final Pattern patternCountry = Pattern.compile(regexCountry);

    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private MovieRepository movieRepository;

    public void setCountryRepository(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    public void init() throws Exception {
        URL url = ImdbMovieListService.class.getClassLoader().getResource("countries.list");
        downloadFtp();
        unzipFile();
        List<Movie> result = parseImdbMovieList(url);
        movieRepository.save(result);
    }

    void downloadFtp() {
        FileOutputStream fileOutputStream = null;
        FTPClient ftpClient = null;
        try {
            ftpClient = new FTPClient();
            ftpClient.connect("ftp.sunet.se");

            fileOutputStream = new FileOutputStream("/Users/seppa/Workspace/worldinmovies/downloaded.gz");
            ftpClient.retrieveFile("/pub/tv+movies/imdb/countries.list.gz", fileOutputStream);
        } catch (IOException e) {
            System.out.println("You suck!");
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                ftpClient.disconnect();
            } catch (IOException e) {
                System.out.println("FUCK!");
            }
        }
    }

    void unzipFile() {
        try {
            byte[] buffer = new byte[1024];

            GZIPInputStream gzis =
                    new GZIPInputStream(new FileInputStream("/Users/seppa/Workspace/worldinmovies/downloaded.gz"));

            FileOutputStream out =
                    new FileOutputStream("/Users/seppa/Workspace/worldinmovies/downloaded.txt");

            int len;
            while ((len = gzis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            gzis.close();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    List<Movie> parseImdbMovieList(URL url2) throws Exception {
        List<Movie> movieEntityList = new ArrayList<>();

        String path = new File("/Users/seppa/Workspace/worldinmovies/downloaded.txt").getAbsolutePath();
        if("" != null) {
            try {
                Stream<String> result = Files.lines(Paths.get(path), StandardCharsets.ISO_8859_1);

                for (String string : result.skip(16).collect(Collectors.toList())) {
                    if(!string.equals("--------------------------------------------------------------------------------")) {
                        Movie movieEntity = parseMovieEntity(string);
                        movieEntityList.add(movieEntity);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return movieEntityList;
    }

    Movie parseMovieEntity(String line) throws Exception {
        Movie movieEntity = new Movie();

        Matcher nameAndYearMatcher = patternNameAndYear.matcher(line);

        if(nameAndYearMatcher.find()) {
            movieEntity.setName(nameAndYearMatcher.group(1));
            movieEntity.setYear(nameAndYearMatcher.group(2));

            Matcher countryMatcher = patternCountry.matcher(line);

            if(countryMatcher.find()) {
                String countryName = mapCountries(countryMatcher.group(0).trim());
                movieEntity.setCountry(countryRepository.findByName(countryName));
            } else {
                throw new Exception("Coult not parse country: " + line);
            }
        } else {
            throw new Exception("Could not parse line " + line);
        }

        return movieEntity;
    }

    public static String mapCountries(String imdbCountryName) {
        Map<String, String> specialCountries = new HashMap<>();
        specialCountries.put("Netherlands Antilles",    "Netherlands");
        specialCountries.put("Burma",                   "Myanmar");
        specialCountries.put("Ivory Coast",             "Côte d'Ivoire");
        specialCountries.put("Czechoslovakia",          "Czech Republic");
        specialCountries.put("Kosovo",                  "Serbia");
        specialCountries.put("Laos",                    "Lao People's Democratic Republic");
        specialCountries.put("Reunion",                 "Réunion");
        specialCountries.put("Siam",                    "Thailand");
        specialCountries.put("UK",                      "United Kingdom");
        specialCountries.put("USA",                     "United States");
        specialCountries.put("Soviet Union",            "Russian Federation");
        specialCountries.put("Vietnam",                 "Viet nam");
        specialCountries.put("Yugoslavia",              "Serbia");
        specialCountries.put("Zaire",                   "Congo, the Democratic Republic of the");

        String result = specialCountries.get(imdbCountryName);

        return result != null ? result : imdbCountryName;
    }

}
