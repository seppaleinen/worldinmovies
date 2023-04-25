package se.worldinmovies.neo4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.worldinmovies.neo4j.xml.LanguageMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LanguageTest {
    private LanguageMapper languageMapper;

    @BeforeEach
    public void setup() {
        this.languageMapper = new LanguageMapper();
    }
    @Test
    void testSE() {
        List<String> language = languageMapper.getLanguagesFromCountryCode("SE");
        assertEquals(2, language.size(), language.toString());
        assertTrue(language.stream().anyMatch(a -> a.equals("sv")));
        assertTrue(language.stream().anyMatch(a -> a.equals("fi")));
    }

    @Test
    void testUS() {
        List<String> language = languageMapper.getLanguagesFromCountryCode("US");
        assertEquals(3, language.size(), language.toString());
        assertTrue(language.stream().anyMatch(a -> a.equals("en")));
        assertTrue(language.stream().anyMatch(a -> a.equals("es")));
        assertTrue(language.stream().anyMatch(a -> a.equals("haw")));
    }

    @Test
    void testJP() {
        List<String> language = languageMapper.getLanguagesFromCountryCode("JP");
        assertEquals(1, language.size(), language.toString());
        assertTrue(language.stream().anyMatch(a -> a.equals("ja")));
    }

    @Test
    void testCH() {
        List<String> language = languageMapper.getLanguagesFromCountryCode("CH");
        assertEquals(5, language.size(), language.toString());
        assertTrue(language.stream().anyMatch(a -> a.equals("de")));
        assertTrue(language.stream().anyMatch(a -> a.equals("gsw")));
        assertTrue(language.stream().anyMatch(a -> a.equals("rm")));
        assertTrue(language.stream().anyMatch(a -> a.equals("it")));
        assertTrue(language.stream().anyMatch(a -> a.equals("fr")));
    }
    
    @Test
    void testAC() {
        List<String> language = languageMapper.getLanguagesFromCountryCode("AC");
        assertEquals(1, language.size(), language.toString());
        assertTrue(language.stream().anyMatch(a -> a.equals("en")));
    }
}
