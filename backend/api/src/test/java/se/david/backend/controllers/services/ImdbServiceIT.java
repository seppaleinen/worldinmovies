package se.david.backend.controllers.services;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.multipart.MultipartFile;
import se.david.backend.WorldInMoviesApplication;
import se.david.backend.controllers.repository.MovieRepository;
import se.david.commons.Movie;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {WorldInMoviesApplication.class})
// NOTE!! order is important
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class ImdbServiceIT {
    @Autowired
    private ImdbService imdbService;
    @Autowired
    private MovieRepository movieRepository;

    @Before
    public void setup() {
    }

    @Test
    public void canParseUserRatingsFileFromImdb() throws IOException {
        Movie movieEntity = new Movie();
        movieEntity.setName("Time of the Wolf");
        movieEntity.setYear("2003");
        movieEntity.setCountry("country");
        movieEntity.setId(movieEntity.getName() + ":" + movieEntity.getYear() + ":country");
        movieRepository.save(movieEntity);

        String path = ImdbServiceIT.class.getClassLoader().getResource("small_ratings.csv").getPath();
        File file = new File(path);

        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile("file",
                file.getName(), "text/plain", IOUtils.toByteArray(input));

        List<Movie> result = imdbService.parseFromUserRatingsFile(multipartFile);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Time of the Wolf", result.get(0).getName());
        assertEquals("2003", result.get(0).getYear());
    }
}
