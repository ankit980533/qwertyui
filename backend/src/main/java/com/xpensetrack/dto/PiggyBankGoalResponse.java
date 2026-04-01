package com.xpensetrack.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class PiggyBankGoalResponse {
    private String id;
    private String goalName;
    private double targetAmount;
    private double savedAmount;
    private LocalDate deadline;
    private double progressPercent;
    private double dailySavingNeeded;
    private String imageUrl;
}
