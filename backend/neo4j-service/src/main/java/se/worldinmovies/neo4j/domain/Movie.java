package se.worldinmovies.neo4j.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Movie {
	@JsonProperty(value = "id")
	private Integer movieId;
	@JsonProperty(value = "imdb_id")
	private String imdbId;
	@JsonProperty(value = "original_title")
	private String originalTitle;
	@JsonProperty(value = "title")
	private String engTitle;
	@JsonProperty(value = "spoken_languages")
	private List<Language> spokenLanguages = new ArrayList<>();
	@JsonProperty(value = "production_countries")
	private List<Country> producedBy = new ArrayList<>();
	@JsonProperty(value = "genres")
	private List<Genre> genres = new ArrayList<>();
}
