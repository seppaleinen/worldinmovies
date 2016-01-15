package se.david.labs;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KrogRouletteRepository extends MongoRepository<KrogRouletteEntity, String> {
}
