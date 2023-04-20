package se.worldinmovies.neo4j.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
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
    @JsonProperty(value = "poster_path")
    private String posterPath;
    @JsonProperty(value = "vote_average")
    private double voteAverage;
    @JsonProperty(value = "vote_count")
    private int voteCount;
    @JsonProperty(value = "alternative_titles")
    private AltTitles alternativeTitles;

    /**
     * The formula for calculating the Top Rated 250 Titles gives a true Bayesian estimate:
     * weighted rating (WR) = (v ÷ (v+m)) × R + (m ÷ (v+m)) × C where:
     * <p>
     * R = average for the movie (mean) = (Rating)
     * v = number of votes for the movie = (votes)
     * m = minimum votes required to be listed in the Top 250 (currently 25000)
     * C = the mean vote across the whole report (currently 7.0)
     **/
    public double calculateWeightedRating() {
        if(voteAverage == 0 || voteCount == 0) return 0;
        double r = voteAverage;
        int v = voteCount;
        double m = 200;
        int c = 4;
        return (v / (v + m)) * r + (m / (v + m)) * c;
    }

    public Optional<String> guessEnglishTitle() {
        return alternativeTitles.alternativTitles()
                .stream()
                .filter(a -> List.of("US", "GB").contains(a.iso()))
                .map(AltTitles.AlternativTitle::title)
                .findFirst()
                .or(() -> Optional.ofNullable(this.engTitle));
    }
}
