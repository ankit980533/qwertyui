package com.xpensetrack.repository;

import com.xpensetrack.model.Expense;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.time.Instant;
import java.util.List;

public interface ExpenseRepository extends MongoRepository<Expense, String> {
    List<Expense> findByUserIdOrderByDateDesc(String userId);

    @Query("{ 'userId': ?0, 'date': { '$gte': ?1, '$lt': ?2 } }")
    List<Expense> findByUserIdAndDateRange(String userId, Instant start, Instant end);
}
