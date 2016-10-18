package se.david.backend.controllers.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.core.io.PathResource;
import se.david.backend.controllers.repository.MovieRepository;
import se.david.backend.controllers.repository.entities.Movie;
import se.david.backend.controllers.services.util.ImdbInterface;
import se.david.backend.controllers.services.util.ImdbProcessor;

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
    private ImdbProcessor imdbProcessor = new ImdbProcessor();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test_save_small_list() {
        String path = ImportService.class.getClassLoader().getResource("countries.small.list").getPath();
        PathResource resource = new PathResource(path);
        when(imdbInterface.getCountriesResource()).thenReturn(resource);

        importService.importImdbCountries();

        verify(imdbInterface, times(1)).getCountriesResource();
        verify(imdbProcessor, atLeastOnce()).process(anyListOf(String.class));
        verify(movieRepository, atLeastOnce()).save(anySetOf(Movie.class));
    }
}
