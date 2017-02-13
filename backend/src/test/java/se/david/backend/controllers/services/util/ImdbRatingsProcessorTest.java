package se.david.backend.controllers.services.util;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import se.david.backend.repository.MovieRepository;
import se.david.backend.domain.Movie;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;

public class ImdbRatingsProcessorTest {
    @InjectMocks
    private ImdbRatingsProcessor imdbRatingsProcessor;
    @Mock
    private MovieRepository movieRepository;

    private static final String NUMBER_REGEX = "^[\\d\\?\\./IVX]+$";
    private static final Pattern NUMBER_PATTERN = Pattern.compile(NUMBER_REGEX);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test_trickyRegexCase() {
        String row = "      200..0.005      36   7.0  W (2014/I)";

        Movie movie = Movie.builder().
                name("NAME").
                year("1234").
                id("NAME:1234").
                build();

        Mockito.when(movieRepository.findOne(anyString())).thenReturn(movie);

        Movie result = imdbRatingsProcessor.process(row);

        assertNotNull("Movie should not be null", result);
        assertEquals("NAME", result.getName());
        assertEquals("1234", result.getYear());
        assertEquals("7.0", result.getRating());
        assertEquals("36", result.getVotes());
        assertEquals("0.6528497409326425", result.getWeightedRating());

        String row2 = "      2111100001     155   3.7  9 Days (2013)";

        Movie result2 = imdbRatingsProcessor.process(row2);

        assertNotNull("Movie should not be null", result2);
        assertEquals("NAME", result2.getName());
        assertEquals("1234", result2.getYear());
        assertEquals("3.7", result2.getRating());
        assertEquals("155", result2.getVotes());
        assertEquals("1.1356435643564358", result2.getWeightedRating());

    }

    @Test
    public void test_random_list() {
        URL resource = ImdbCountryProcessorTest.class.getClassLoader().getResource("ratings.random.list");

        Movie movie = Movie.builder().
                        name("NAME").
                        year("1234").
                        id("NAME:1234").
                        build();

        Mockito.when(movieRepository.findOne(anyString())).thenReturn(movie);
        assertNotNull(resource);

        try (Stream<String> stream = Files.lines(Paths.get(resource.getPath()), StandardCharsets.ISO_8859_1)) {
            for(String row: stream.collect(Collectors.toList())) {
                if(!row.contains("\"")) {
                    Movie result = imdbRatingsProcessor.process(row);
                    assertNotNull("movie should not be null for line: " + row, result);
                    assertNotNull("ID should not be null for: " + row + ": " + result.toString(), result.getId());
                    assertNotNull("Name should not be null for: " + row + ": " + result.toString(), result.getName());
                    assertNotNull("Year should not be null for: " + row + ": " + result.toString(), result.getYear());
                    assertTrue("Year should only contain numbers and questionmarks: " + result.getYear(), NUMBER_PATTERN.matcher(result.getYear()).matches());
                    assertTrue("Ratings should only contain numbers: " + result.getRating(), NUMBER_PATTERN.matcher(result.getRating()).matches());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Should not fail " + e.getMessage());
        }
    }

    @Ignore
    @Test
    public void test_full_list() {
        URL resource = ImdbCountryProcessorTest.class.getClassLoader().getResource("ratings.list");

        assertNotNull(resource);

        try (Stream<String> stream = Files.lines(Paths.get(resource.getPath()), StandardCharsets.ISO_8859_1)) {
            for(String row: stream.skip(296).collect(Collectors.toList())) {
                if(!row.contains("\"")) {
                    Movie result = imdbRatingsProcessor.process(row);
                    if(result != null) {
                        assertNotNull("movie should not be null for line: " + row, result);
                        assertNotNull("ID should not be null for: " + row + ": " + result.toString(), result.getId());
                        assertNotNull("Name should not be null for: " + row + ": " + result.toString(), result.getName());
                        assertNotNull("Year should not be null for: " + row + ": " + result.toString(), result.getYear());
                        assertTrue("Year should only contain numbers and questionmarks: " + result.getYear(), NUMBER_PATTERN.matcher(result.getYear()).matches());
                        assertTrue("Ratings should only contain numbers: " + result.getRating(), NUMBER_PATTERN.matcher(result.getRating()).matches());
                    } else {
                        System.out.println("Row resulted in null: " + row);
                    }
                }
            }
        } catch (Exception e) {
            fail("Should not fail" + e.getMessage());
        }
    }
}
