package com.xpensetrack.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Data
@Document(collection = "piggy_banks")
public class PiggyBank {
    @Id
    private String id;
    private String userId;
    private String goalName;
    private double targetAmount;
    private double savedAmount = 0.0;
    private LocalDate deadline;
    private String imageUrl;
    private LocalDate createdAt = LocalDate.now();

    public double getDailySavingNeeded() {
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), deadline);
        return daysLeft > 0 ? (targetAmount - savedAmount) / daysLeft : 0.0;
    }

    public double getProgressPercent() {
        return targetAmount > 0 ? (savedAmount / targetAmount) * 100 : 0.0;
    }
}
