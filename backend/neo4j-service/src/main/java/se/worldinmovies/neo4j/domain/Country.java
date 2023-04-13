package se.worldinmovies.neo4j.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Country {
	@JsonProperty(value = "iso_3166_1")
	private String iso;
	private String name;
	@JsonProperty(value = "spoken_languages")
	private List<Language> languages = new ArrayList<>();
}
