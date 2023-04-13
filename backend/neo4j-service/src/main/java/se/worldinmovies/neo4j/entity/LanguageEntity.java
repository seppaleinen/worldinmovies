package se.worldinmovies.neo4j.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.io.Serializable;

@Node(labels = "Language")
@Data
@NoArgsConstructor
public class LanguageEntity implements Serializable {
    @Id
    private String iso;
    private String name;
    private String englishName;

    public LanguageEntity(String iso, String name, String englishName) {
        this.iso = iso;
        this.name = name;
        this.englishName = englishName;
    }
}
