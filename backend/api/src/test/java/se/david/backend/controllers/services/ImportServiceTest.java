package se.david.backend.controllers.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.core.io.PathResource;
import org.springframework.test.util.ReflectionTestUtils;
import se.david.backend.controllers.repository.MovieRepository;
import se.david.backend.controllers.repository.entities.Movie;
import se.david.backend.controllers.services.util.ImdbInterface;
import se.david.backend.controllers.services.util.ImdbCountryProcessor;
import se.david.backend.controllers.services.util.ImdbRatingsProcessor;

import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.*;

public class ImportServiceTest {
    @InjectMocks
    private ImportService importService;
    @Mock
    private ImdbInterface imdbInterface;
    @Mock
    private MovieRepository movieRepository;
    @Spy
    private ImdbCountryProcessor imdbCountryProcessor = new ImdbCountryProcessor();
    @Spy
    private ImdbRatingsProcessor imdbRatingsProcessor = new ImdbRatingsProcessor();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(imdbCountryProcessor, "movieRepository", movieRepository);
        ReflectionTestUtils.setField(imdbRatingsProcessor, "movieRepository", movieRepository);
    }

    @Test
    public void test_save_small_list() {
        String path = ImportService.class.getClassLoader().getResource("countries.small.list").getPath();
        PathResource resource = new PathResource(path);
        when(imdbInterface.getCountriesResource()).thenReturn(resource);

        importService.importImdbCountries();

        verify(imdbInterface, times(1)).getCountriesResource();
        verify(imdbCountryProcessor, atLeastOnce()).process(anyListOf(String.class));
        verify(movieRepository, atLeastOnce()).save(anySetOf(Movie.class));
    }

    @Test
    public void test_save_random_ratings_list() {
        String path = ImportService.class.getClassLoader().getResource("ratings.random.list").getPath();
        PathResource resource = new PathResource(path);
        when(imdbInterface.getRatingsResource()).thenReturn(resource);

        importService.importImdbRatings();

        verify(imdbInterface, times(1)).getRatingsResource();
        verify(imdbRatingsProcessor, atLeastOnce()).process(anyListOf(String.class));
        verify(movieRepository, atLeastOnce()).save(anySetOf(Movie.class));
    }
}
