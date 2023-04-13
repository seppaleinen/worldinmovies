package se.worldinmovies.neo4j.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node(labels = "Person")
public class PersonEntity {
    @Id
    @JsonProperty(value = "id")
    private Integer id;
    //@Version
    //@JsonIgnore
    //private Long version;
    private String name;
    private double popularity;
    private String poster;
    //TODO Should not be here, but on direction maybe?
    //private String job;
}
