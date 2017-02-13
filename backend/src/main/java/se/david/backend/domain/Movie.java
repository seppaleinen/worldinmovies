package se.david.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Document
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    @Id
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String id;
    private String name;
    private String year;
    @Indexed
    private Set<String> countrySet = new HashSet<>();
    private String rating;
    @JsonIgnore
    private String votes;
    @Indexed
    @JsonIgnore
    private String weightedRating;
}
