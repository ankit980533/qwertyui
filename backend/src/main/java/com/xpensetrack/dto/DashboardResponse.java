package com.xpensetrack.dto;

import com.xpensetrack.model.ExpenseCategory;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardResponse {
    private String fullName;
    private double currentBalance;
    private double monthlyBudget;
    private double monthlySpent;
    private double remaining;
    private double budgetUsedPercent;
    private double budgetLeftPercent;
    private int coins;
    private List<ExpenseResponse> recentExpenses;
    private int dragonLevel;
    private int dragonHappiness;
    private boolean dragonHungry;
    private Map<ExpenseCategory, Double> monthlyBreakdown;
    private int unreadNotificationCount;
}
