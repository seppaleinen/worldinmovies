package se.david.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import se.david.backend.domain.Country;

@Repository
public interface CountryRepository extends MongoRepository<Country, String> {
    Country findByName(String name);
}
