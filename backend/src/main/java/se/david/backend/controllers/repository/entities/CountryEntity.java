package se.david.backend.controllers.repository.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "CountryEntity")
@Data
public class CountryEntity {
    @Id
    private String id;
    private String code;
    private String name;
    private List<MovieEntity> movieEntityList;
}
