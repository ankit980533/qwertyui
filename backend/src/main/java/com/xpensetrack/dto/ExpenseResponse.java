package com.xpensetrack.dto;

import com.xpensetrack.model.ExpenseCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ExpenseResponse {
    private String id;
    private double amount;
    private String description;
    private ExpenseCategory category;
    private String note;
    private LocalDate date;
}
