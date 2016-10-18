package se.david.backend.controllers.services;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.collections.Sets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import se.david.backend.controllers.repository.MovieRepository;
import se.david.backend.controllers.repository.entities.Movie;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ImdbServiceTest {
    @InjectMocks
    private ImdbService imdbService;
    @Mock
    private MovieRepository movieRepository;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void canParseUserRatingsFileFromImdb() throws IOException {
        Movie movieEntity = Movie.builder().
                                name("Time of the Wolf").
                                year("2003").
                                id("Time of the Wolf:2003:country").
                                build();
        movieEntity.setCountrySet(Sets.newSet("country"));
        when(movieRepository.findByIdRegex(anyString())).thenReturn(Collections.singletonList(movieEntity));

        InputStream file = new ClassPathResource("small_ratings.csv").getInputStream();

        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                "filename",
                "text/plain",
                IOUtils.toByteArray(file));

        List<Movie> result = imdbService.parseFromUserRatingsFile(multipartFile);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Time of the Wolf", result.get(0).getName());
        assertEquals("2003", result.get(0).getYear());
        verify(movieRepository, times(1)).findByIdRegex(eq("Time of the Wolf:2003"));
    }

    @Test
    public void canGetMoviesByCountry() {
        Movie movieEntity = Movie.builder().
                name("Time of the Wolf").
                year("2003").
                id("Time of the Wolf:2003:SE").
                build();
        movieEntity.setCountrySet(Sets.newSet("SE"));
        when(movieRepository.findTop5ByCountrySet(anyString(), any(Pageable.class))).thenReturn(Collections.singletonList(movieEntity));

        String countryCode = "SE";

        List<Movie> result = imdbService.getMoviesByCountry(countryCode);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(movieEntity.getId(), result.get(0).getId());
        verify(movieRepository, times(1)).findTop5ByCountrySet("SE", new PageRequest(0, 5));
    }
}
