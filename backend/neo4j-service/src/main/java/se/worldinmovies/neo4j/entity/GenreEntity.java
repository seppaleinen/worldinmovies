package se.worldinmovies.neo4j.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Node("Genre")
@Data
@NoArgsConstructor
public class GenreEntity implements Serializable {
    @Id
    private Integer id;
    private String name;
    private long version;

    public GenreEntity(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}
