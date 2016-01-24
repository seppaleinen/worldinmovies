package se.david.batch.job.country;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import se.david.commons.Country;

@Repository
public interface CountryRepository extends MongoRepository<Country, String> {
    Country findByName(String name);
    Country findByCode(String code);
}
