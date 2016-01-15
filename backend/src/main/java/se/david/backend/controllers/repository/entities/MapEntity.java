package se.david.backend.controllers.repository.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class MapEntity {
    @Id
    private String id;
    private String param;
}
