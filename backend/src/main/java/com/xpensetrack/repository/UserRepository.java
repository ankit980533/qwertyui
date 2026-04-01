package com.xpensetrack.repository;

import com.xpensetrack.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    @Query("{ '$or': [ { 'fullName': { '$regex': ?0, '$options': 'i' } }, { '_id': { '$regex': ?0, '$options': 'i' } } ] }")
    List<User> searchByNameOrId(String query);
}
