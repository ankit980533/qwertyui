package com.xpensetrack.repository;

import com.xpensetrack.model.Expense;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends MongoRepository<Expense, String> {
    List<Expense> findByUserIdOrderByDateDesc(String userId);
    List<Expense> findByUserIdAndDateBetween(String userId, LocalDate start, LocalDate end);
    List<Expense> findByUserIdAndDate(String userId, LocalDate date);
}
