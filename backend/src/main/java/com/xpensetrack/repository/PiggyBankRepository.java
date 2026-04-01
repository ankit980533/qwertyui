package com.xpensetrack.repository;

import com.xpensetrack.model.PiggyBank;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface PiggyBankRepository extends MongoRepository<PiggyBank, String> {
    List<PiggyBank> findByUserId(String userId);
}
