package se.david.backend.controllers;

import org.junit.Before;
import org.junit.Test;
import se.david.backend.controllers.repository.entities.MovieEntity;

import java.io.File;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class ImdbControllerIT {
    private ImdbController imdbController;

    @Before
    public void setup() {
        imdbController = new ImdbController();
    }

    @Test
    public void canReadCountriesList() {
        URL url = ImdbControllerIT.class.getClassLoader().getResource("countries.list");

        List<MovieEntity> result = imdbController.parseImdbMovieList(url);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void canParseLinesFromCountriesList() {
        String line = "Goosebumps: Escape from Horrorland (1996) (VG)\t\tUSA\t";

        MovieEntity result = imdbController.parseMovieEntity(line);

        assertNotNull("Result should not be null", result);
        assertEquals("Goosebumps: Escape from Horrorland ", result.getName());
        assertEquals("1996", result.getYear());
    }

    @Test
    public void canGetRssFeedFromImdb() {
        String path = ImdbControllerIT.class.getClassLoader().getResource("ratings.csv").getPath();
        File file = new File(path);
        List<MovieEntity> result = imdbController.getRssFeed(file);

        assertNotNull(result);
        assertEquals(1818, result.size());
        assertEquals("Time of the Wolf", result.get(0).getName());
        assertEquals("2003", result.get(0).getYear());
    }
}
