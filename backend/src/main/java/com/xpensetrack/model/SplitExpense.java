package com.xpensetrack.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.List;

@Data
@Document(collection = "split_expenses")
public class SplitExpense {
    @Id
    private String id;
    private String groupId;
    private String paidByUserId;
    private String description;
    private double totalAmount;
    private List<Split> splits;
    private Instant createdAt = Instant.now();
}
