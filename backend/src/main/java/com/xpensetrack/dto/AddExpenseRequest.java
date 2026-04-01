package com.xpensetrack.dto;

import com.xpensetrack.model.ExpenseCategory;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class AddExpenseRequest {
    @Positive private double amount;
    private String description;
    private ExpenseCategory category;
    private String note;
    private LocalDate date;
    private List<String> splitWithFriendIds = new ArrayList<>();
}
