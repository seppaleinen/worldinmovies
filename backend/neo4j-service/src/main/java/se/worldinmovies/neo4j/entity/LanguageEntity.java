package se.worldinmovies.neo4j.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.io.Serializable;

@Node(value = "Language", labels = "Language")
@Data
@NoArgsConstructor
public class LanguageEntity implements Serializable {
    @Id
    @Property
    private String iso;
    @Property
    private String name;
    @Property
    private String englishName;
}
