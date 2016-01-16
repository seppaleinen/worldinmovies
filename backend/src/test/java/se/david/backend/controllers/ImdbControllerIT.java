package se.david.backend.controllers;

import org.junit.Before;
import org.junit.Test;
import se.david.backend.controllers.repository.entities.MovieEntity;

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
        List<MovieEntity> result = imdbController.parseImdbMovieList();

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
}
