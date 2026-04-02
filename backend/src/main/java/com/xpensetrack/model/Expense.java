package com.xpensetrack.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "expenses")
public class Expense {
    @Id
    private String id;
    private String userId;
    private double amount;
    private String description;
    private ExpenseCategory category;
    private String note;
    private Instant date;  // UTC timestamp
    private List<String> splitWithFriendIds = new ArrayList<>();
    private Instant createdAt = Instant.now();
}
