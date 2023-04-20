package se.worldinmovies.neo4j.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AltTitles(@JsonProperty(value = "titles") List<AlternativTitle> alternativTitles) {
    public record AlternativTitle(@JsonProperty(value = "iso_3166_1") String iso,
                                  @JsonProperty("title") String title) {
    }
}
