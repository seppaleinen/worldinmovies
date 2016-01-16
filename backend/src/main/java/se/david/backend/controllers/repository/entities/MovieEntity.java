package se.david.backend.controllers.repository.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;

@Document
@Data
public class MovieEntity {
    @Id
    private String id;
    private String name;
    private String year;
    private CountryEntity countryEntity;
}
