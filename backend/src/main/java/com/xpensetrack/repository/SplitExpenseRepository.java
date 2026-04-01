package com.xpensetrack.repository;

import com.xpensetrack.model.SplitExpense;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface SplitExpenseRepository extends MongoRepository<SplitExpense, String> {
    List<SplitExpense> findByPaidByUserId(String userId);
    List<SplitExpense> findBySplitsUserId(String userId);
}
