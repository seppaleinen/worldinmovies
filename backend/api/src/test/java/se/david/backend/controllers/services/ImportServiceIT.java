package se.david.backend.controllers.services;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.PathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import se.david.backend.WorldInMoviesApplication;
import se.david.backend.controllers.repository.MovieRepository;
import se.david.backend.controllers.services.util.ImdbInterface;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {WorldInMoviesApplication.class},
        properties = "classpath:application-test.properties")
@ContextConfiguration(classes = WorldInMoviesApplication.class)
public class ImportServiceIT {
    @Autowired
    private ImportService importService;
    @Mock
    private ImdbInterface imdbInterface;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private ImdbInterface realImdbInterface;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        movieRepository.deleteAll();
        ReflectionTestUtils.setField(importService, "imdbInterface", imdbInterface);
    }

    @Test
    public void test_save_small_list() {
        String path = ImportService.class.getClassLoader().getResource("countries.small.list").getPath();
        PathResource resource = new PathResource(path);
        when(imdbInterface.getCountriesResource()).thenReturn(resource);

        assertEquals(0, movieRepository.count());

        importService.importImdbCountries();

        assertEquals(1804, movieRepository.count());
    }

    @Ignore("Imports all of imdbs country list")
    @Test
    public void test_all_imdb() {
        ReflectionTestUtils.setField(importService, "imdbInterface", realImdbInterface);

        assertEquals(0, movieRepository.count());

        importService.importImdbCountries();

        assertEquals(1461606, movieRepository.count());
    }

}
