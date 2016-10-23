package se.david.backend.controllers.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import se.david.backend.controllers.repository.entities.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
}
