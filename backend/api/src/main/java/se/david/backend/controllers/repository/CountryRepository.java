package se.david.backend.controllers.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import se.david.backend.controllers.repository.entities.Country;

@Repository
public interface CountryRepository extends MongoRepository<Country, String> {
    Country findByName(String name);
}
