package com.xpensetrack.dto;

import com.xpensetrack.model.ExpenseCategory;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class AddExpenseRequest {
    @Positive private double amount;
    private String description;
    private ExpenseCategory category;
    private String note;
    private String date; // ISO-8601 UTC string e.g. "2026-04-01T00:00:00Z"
    private List<String> splitWithFriendIds = new ArrayList<>();
}
