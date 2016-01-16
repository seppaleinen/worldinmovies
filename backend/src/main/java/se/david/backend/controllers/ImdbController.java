package se.david.backend.controllers;

import lombok.extern.java.Log;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import se.david.backend.controllers.repository.entities.MovieEntity;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log
public class ImdbController {
    public List<MovieEntity> parseImdbMovieList(){
        List<MovieEntity> movieEntityList = new ArrayList<>();

        URL url = ImdbController.class.getClassLoader().getResource("countries.list");
        if(url != null) {
            try {
                Stream<String> result = Files.lines(Paths.get(url.getPath()), StandardCharsets.ISO_8859_1);

                for (String string : result.collect(Collectors.toList())) {
                    MovieEntity movieEntity = parseMovieEntity(string);
                    movieEntityList.add(movieEntity);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return movieEntityList;
    }

    private static final String regex = "([a-zA-Z\\: ]*).*([0-9]{4,4}).*";
    private static final Pattern pattern = Pattern.compile(regex);


    public MovieEntity parseMovieEntity(String line) {
        MovieEntity movieEntity = null;

        Matcher matcher = pattern.matcher(line);

        if(matcher.find()) {
            movieEntity = new MovieEntity();
            movieEntity.setName(matcher.group(1));
            movieEntity.setYear(matcher.group(2));
        }

        return movieEntity;
    }
}
