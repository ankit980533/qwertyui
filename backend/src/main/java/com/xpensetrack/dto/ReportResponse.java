package com.xpensetrack.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ReportResponse {
    private String period;
    private double totalSpent;
    private double totalSaved;
    private double savedChangePercent;
    private List<TrendPoint> monthlySpendingTrend;
    private List<TrendPoint> weeklySpending;
    private List<TrendPoint> savingsTrend;
}
