package se.worldinmovies.neo4j.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Node(labels = "Country")
@Data
@NoArgsConstructor
public class CountryEntity implements Serializable {
    @Id
    private String iso;
    private String name;
    @Relationship(direction = Relationship.Direction.OUTGOING)
    private List<LanguageEntity> languages = new ArrayList<>();

    public CountryEntity(String iso, String name, List<LanguageEntity> languages) {
        this.iso = iso;
        this.name = name;
        this.languages = languages;
    }
}
