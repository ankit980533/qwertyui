package com.xpensetrack.dto;

import com.xpensetrack.model.ExpenseCategory;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExpenseResponse {
    private String id;
    private double amount;
    private String description;
    private ExpenseCategory category;
    private String note;
    private String date; // ISO-8601 UTC string
}
