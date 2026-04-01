package com.xpensetrack.repository;

import com.xpensetrack.model.Dragon;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface DragonRepository extends MongoRepository<Dragon, String> {
    Optional<Dragon> findByUserId(String userId);
}
