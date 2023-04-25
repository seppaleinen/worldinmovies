package se.worldinmovies.neo4j;

import com.github.tomakehurst.wiremock.common.Json;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.worldinmovies.neo4j.domain.Country;
import se.worldinmovies.neo4j.domain.Genre;
import se.worldinmovies.neo4j.domain.Language;
import wiremock.com.fasterxml.jackson.databind.JsonNode;
import wiremock.com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class GenerateCyphers {
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        this.objectMapper = Json.getObjectMapper();
    }
    @SneakyThrows
    @Test
    public void genres() {
        String data = getString("genres.json");

        Genre[] genres = objectMapper.reader()
                .readValue(data, Genre[].class);

        Arrays.stream(genres)
                .sorted(Comparator.comparing(Genre::getId))
                .forEach(a -> System.out.printf("CREATE (n:Genre {id: %s, name: '%s'});%n", a.getId(), a.getName()));
    }

    @SneakyThrows
    @Test
    public void languages() {
        String data = getString("languages.json");

        Language[] genres = objectMapper.reader()
                .readValue(data, Language[].class);

        Arrays.stream(genres)
                .sorted(Comparator.comparing(Language::getIso))
                .forEach(a -> System.out.printf("CREATE (n:Language {iso: '%s', name: '%s'});%n", a.getIso(), a.getName()));
    }

    @SneakyThrows
    @Test
    public void countries() {
        String data = getString("countries.json");

        Country[] genres = objectMapper.reader()
                .readValue(data, Country[].class);

        Arrays.stream(genres)
                .sorted(Comparator.comparing(Country::getIso))
                .forEach(a -> System.out.printf("CREATE (n:Country {iso: '%s', name: '%s'});%n", a.getIso(), a.getName()));
    }

    @SneakyThrows
    private String getString(String filename) {
        URL resourceFile = Neo4JIntegrationTest.class.getClassLoader().getResource(filename);
        return java.nio.file.Files.readString(Paths.get(resourceFile.toURI()));
    }
}
