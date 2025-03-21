package it.unipi.lsmsd.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import it.unipi.lsmsd.model.User;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
}