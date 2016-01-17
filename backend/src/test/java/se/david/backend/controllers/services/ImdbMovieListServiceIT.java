package se.david.backend.controllers.services;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.david.backend.controllers.repository.entities.MovieEntity;

import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class ImdbMovieListServiceIT {
    private ImdbMovieListService imdbMovieListService;

    @Before
    public void setup() {
        imdbMovieListService = new ImdbMovieListService();
    }

    @Test
    public void canReadCountriesList() throws Exception {
        URL url = ImdbMovieListServiceIT.class.getClassLoader().getResource("countries.list");

        List<MovieEntity> result = imdbMovieListService.parseImdbMovieList(url);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for(MovieEntity movieEntity : result) {
            assertNotNull("MovieEntity should not be null", movieEntity);
            assertNotNull("Name should not be null in " + movieEntity, movieEntity.getName());
            assertNotNull("Year should not be null in " + movieEntity, movieEntity.getYear());
            assertNotNull("CountryEntity should not be null in " + movieEntity, movieEntity.getCountryEntity());
            assertNotNull("CountryName should not be null in " + movieEntity, movieEntity.getCountryEntity().getName());
        }
    }

    @Test
    public void canParseLinesFromCountriesList() throws Exception {
        String line = "Goosebumps: Escape from Horrorland (1996) (VG)\t\tUSA\t";

        MovieEntity result = imdbMovieListService.parseMovieEntity(line);

        assertNotNull("Result should not be null", result);
        assertEquals("Goosebumps: Escape from Horrorland", result.getName());
        assertEquals("1996", result.getYear());
        assertNotNull("CountryEntity should not be null", result.getCountryEntity());
        assertEquals("USA", result.getCountryEntity().getName());
    }
}
