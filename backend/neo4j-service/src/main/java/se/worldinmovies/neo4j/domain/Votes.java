package se.worldinmovies.neo4j.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Votes (int id,
                     @JsonProperty(value = "vote_average") double voteAverage,
                     @JsonProperty(value = "vote_count") int voteCount,
                     @JsonProperty(value = "imdb_vote_average") double imdbVoteAverage,
                     @JsonProperty(value = "imdb_vote_count") int imdbVoteCount,
                     @JsonProperty(value = "weighted_rating") double weightedRating) {
}
