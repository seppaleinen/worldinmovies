package se.worldinmovies.neo4j.entity;

import lombok.Data;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
@Data
public class LanguageRelations {
    @RelationshipId
    private Long id;

    @ReadOnlyProperty
    @TargetNode
    private final LanguageEntity language;

}
