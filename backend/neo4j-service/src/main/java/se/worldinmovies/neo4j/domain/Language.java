package se.worldinmovies.neo4j.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Language {
    @JsonProperty(value = "iso_639_1")
    private String iso;
    private String name;
    @JsonProperty(value = "english_name")
    private String englishName;
}
