package se.worldinmovies.neo4j.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.io.Serializable;

@Node(value = "Country", labels = "Country")
@Data
@NoArgsConstructor
public class CountryEntity implements Serializable {
    @Id
    @Property
    private String iso;
    @Property
    private String name;
}
