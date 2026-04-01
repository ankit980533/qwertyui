package com.xpensetrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class PiggyBankOverviewResponse {
    private double monthlySavings;
    private double savingsTarget;
    private double savingsProgressPercent;
    private List<PiggyBankGoalResponse> recentGoals;
}
