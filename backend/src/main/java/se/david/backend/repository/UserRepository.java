package se.david.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import se.david.backend.domain.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
}
