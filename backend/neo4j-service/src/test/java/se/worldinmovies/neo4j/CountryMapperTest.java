package se.worldinmovies.neo4j;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CountryMapperTest {
    @Test
    void russiaShouldIncludeSoviet() {
        List<String> actual = CountryMapper.getOldFromNew("RU");

        assertEquals("RU", actual.get(0));
        assertEquals("SU", actual.get(1));
    }

    @Test
    void countryNotInMapperShouldReturnAsIs() {
        List<String> actual = CountryMapper.getOldFromNew("SE");

        assertEquals("SE", actual.get(0));
        assertEquals(1, actual.size());
    }

}