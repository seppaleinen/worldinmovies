package se.david.backend.controllers.services;

import org.junit.Before;
import org.junit.Test;
import se.david.backend.controllers.repository.entities.MovieEntity;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ImdbUserRatingsServiceIT {
    private ImdbUserRatingsService imdbUserRatingsService;

    @Before
    public void setup() {
        imdbUserRatingsService = new ImdbUserRatingsService();
    }

    @Test
    public void canParseUserRatingsFileFromImdb() {
        String path = ImdbUserRatingsServiceIT.class.getClassLoader().getResource("ratings.csv").getPath();
        File file = new File(path);
        List<MovieEntity> result = imdbUserRatingsService.parseFromUserRatingsFile(file);

        assertNotNull(result);
        assertEquals(1818, result.size());
        assertEquals("Time of the Wolf", result.get(0).getName());
        assertEquals("2003", result.get(0).getYear());
    }

}
