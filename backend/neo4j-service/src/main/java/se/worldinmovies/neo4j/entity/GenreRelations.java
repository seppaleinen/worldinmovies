package se.worldinmovies.neo4j.entity;

import lombok.Data;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.neo4j.core.schema.*;

@RelationshipProperties
@Data
public class GenreRelations {
    @RelationshipId
    private Long id;

    @ReadOnlyProperty
    @TargetNode
    private final GenreEntity genre;
}
