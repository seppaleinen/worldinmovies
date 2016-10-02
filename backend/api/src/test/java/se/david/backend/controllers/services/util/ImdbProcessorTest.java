package se.david.backend.controllers.services.util;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import se.david.commons.Movie;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;

public class ImdbProcessorTest {
    @InjectMocks
    private ImdbProcessor imdbProcessor;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test_process_small_list() {
        URL resource = ImdbProcessorTest.class.getClassLoader().getResource("countries.small.list");

        assertNotNull(resource);

        try (Stream<String> stream = Files.lines(Paths.get(resource.getPath()), StandardCharsets.ISO_8859_1)) {
            for(String row: stream.skip(14).collect(Collectors.toList())) {
                Movie result = imdbProcessor.process(row);
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
        URL resource = ImdbProcessorTest.class.getClassLoader().getResource("countries.random.list");

        assertNotNull(resource);

        try (Stream<String> stream = Files.lines(Paths.get(resource.getPath()), StandardCharsets.ISO_8859_1)) {
            for(String row: stream.collect(Collectors.toList())) {
                Movie result = imdbProcessor.process(row);
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

    @Ignore
    @Test
    public void test_process_faulty_list() {
        URL resource = ImdbProcessorTest.class.getClassLoader().getResource("countries.failing.list");

        assertNotNull(resource);

        try (Stream<String> stream = Files.lines(Paths.get(resource.getPath()), StandardCharsets.ISO_8859_1)) {
            for(String row: stream.skip(14).collect(Collectors.toList())) {
                if(!row.contains("------")) {
                    Movie result = imdbProcessor.process(row);
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
            fail("Should not fail: " + e.getMessage());
        }

    }


}
