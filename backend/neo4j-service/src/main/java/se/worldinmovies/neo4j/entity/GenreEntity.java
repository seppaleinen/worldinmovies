package se.worldinmovies.neo4j.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Node(value = "Genre", labels = "Genre")
@Data
@NoArgsConstructor
public class GenreEntity implements Serializable {
    @Id
    @Property
    private Integer id;
    @Property
    private String name;
    private long version;
}
