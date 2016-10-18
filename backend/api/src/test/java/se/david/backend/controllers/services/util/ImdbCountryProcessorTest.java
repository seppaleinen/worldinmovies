package se.david.backend.controllers.services.util;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import se.david.backend.controllers.repository.entities.Movie;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class ImdbCountryProcessorTest {
    @InjectMocks
    private ImdbCountryProcessor imdbCountryProcessor;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test_process_small_list() {
        URL resource = ImdbCountryProcessorTest.class.getClassLoader().getResource("countries.small.list");

        assertNotNull(resource);

        try (Stream<String> stream = Files.lines(Paths.get(resource.getPath()), StandardCharsets.ISO_8859_1)) {
            for(String row: stream.skip(14).collect(Collectors.toList())) {
                Movie result = imdbCountryProcessor.process(row);
                assertNotNull("movie should not be null for line: " + row, result);
                assertNotNull("ID should not be null for: " + row + ": " + result.toString(), result.getId());
                assertNotNull("Name should not be null for: " + row + ": " + result.toString(), result.getName());
                if(result.getCountry() == null) {
                    System.out.println("Couldn't parse country for row: " + row);
                }
                assertNotNull("Year should not be null for: " + row + ": " + result.toString(), result.getYear());
            }
        } catch (Exception e) {
            fail("Should not fail" + e.getMessage());
        }

    }

    @Test
    public void test_process_random_list() {
        URL resource = ImdbCountryProcessorTest.class.getClassLoader().getResource("countries.random.list");

        assertNotNull(resource);

        try (Stream<String> stream = Files.lines(Paths.get(resource.getPath()), StandardCharsets.ISO_8859_1)) {
            for(String row: stream.collect(Collectors.toList())) {
                Movie result = imdbCountryProcessor.process(row);
                if(result != null) {
                    assertNotNull("movie should not be null for line: " + row, result);
                    assertNotNull("ID should not be null for: " + row + ": " + result.toString(), result.getId());
                    assertNotNull("Name should not be null for: " + row + ": " + result.toString(), result.getName());
                    if (result.getCountry() == null) {
                        System.out.println("Couldn't parse country for row: " + row);
                    }
                    assertNotNull("Year should not be null for: " + row + ": " + result.toString(), result.getYear());
                }
            }
        } catch (Exception e) {
            fail("Should not fail" + e.getMessage());
        }

    }

    @Test
    public void test_process_faulty_list() {
        URL resource = ImdbCountryProcessorTest.class.getClassLoader().getResource("countries.failing.list");

        assertNotNull(resource);

        try (Stream<String> stream = Files.lines(Paths.get(resource.getPath()), StandardCharsets.ISO_8859_1)) {
            for(String row: stream.skip(14).collect(Collectors.toList())) {
                if(!row.contains("------")) {
                    Movie result = imdbCountryProcessor.process(row);
                    if(result != null) {
                        assertNotNull("movie should not be null for line: " + row, result);
                        assertNotNull("ID should not be null for: " + row + ": " + result.toString(), result.getId());
                        assertNotNull("Name should not be null for: " + row + ": " + result.toString(), result.getName());
                        if (result.getCountry() == null) {
                            System.out.println("Couldn't parse country for row: " + row);
                        }
                        assertNotNull("Year should not be null for: " + row + ": " + result.toString(), result.getYear());
                    }
                }
            }
        } catch (Exception e) {
            fail("Should not fail: " + e.getMessage());
        }
    }

    @Test
    public void test_one_failing()  {
        String row = "Å vokte fjellet (2012)\t\t\t\t\tRepublic of Macedonia";

        Movie result = imdbCountryProcessor.process(row);

        assertNotNull("movie should not be null for line: " + row, result);
        assertNotNull("ID should not be null for: " + row + ": " + result.toString(), result.getId());
        assertNotNull("Name should not be null for: " + row + ": " + result.toString(), result.getName());
        if (result.getCountry() == null) {
            System.out.println("Couldn't parse country for row: " + row);
        }
        assertNotNull("Year should not be null for: " + row + ": " + result.toString(), result.getYear());
    }

    @Test
    public void test_process_duplicates_list() {
        URL resource = ImdbCountryProcessorTest.class.getClassLoader().getResource("countries.duplicates.list");

        assertNotNull(resource);

        Set<Movie> movieSet = new HashSet<>();

        try (Stream<String> stream = Files.lines(Paths.get(resource.getPath()), StandardCharsets.ISO_8859_1)) {
            for(String row: stream.collect(Collectors.toList())) {
                Movie result = imdbCountryProcessor.process(row);
                if(result != null) {
                    assertNotNull("movie should not be null for line: " + row, result);
                    assertNotNull("ID should not be null for: " + row + ": " + result.toString(), result.getId());
                    assertNotNull("Name should not be null for: " + row + ": " + result.toString(), result.getName());
                    if (result.getCountry() == null) {
                        System.out.println("Couldn't parse country for row: " + row);
                    }
                    assertNotNull("Year should not be null for: " + row + ": " + result.toString(), result.getYear());
                    movieSet.add(result);
                }
            }
        } catch (Exception e) {
            fail("Should not fail: " + e.getMessage());
        }

        assertEquals(13, movieSet.size());
    }



}
