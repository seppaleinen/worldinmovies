package se.worldinmovies.neo4j.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Genre {
    private Integer id;
    private String name;
}
