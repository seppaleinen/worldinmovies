package se.david.commons;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "MovieEntity")
@Data
public class Movie {
    @Id
    private String id;
    private String name;
    private String year;
    private Country country;
}
