package se.worldinmovies.neo4j.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.io.Serializable;

@Node(labels = "Genre")
@Data
@NoArgsConstructor
public class GenreEntity implements Serializable {
    @Id
    private Integer id;
    private String name;

    public GenreEntity(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}
