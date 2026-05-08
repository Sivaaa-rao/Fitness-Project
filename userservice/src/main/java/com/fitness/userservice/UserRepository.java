package com.fitness.userservice;

import com.fitness.userservice.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Boolean existsByEmail(String email);

    Boolean existsByKeycloakId(String userId);

    User findByEmail(String email);

    Optional<User> findByKeycloakId(String keycloakId);
}
