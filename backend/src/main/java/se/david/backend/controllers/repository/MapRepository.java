package se.david.backend.controllers.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import se.david.backend.controllers.repository.entities.MapEntity;

@Repository
public interface MapRepository extends MongoRepository<MapEntity, String> {
}
