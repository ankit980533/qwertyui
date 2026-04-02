package com.xpensetrack.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {
    private String id;
    private String displayId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private String hostel;
    private String avatarUrl;
    private int coins;
    private double currentBalance;
    private double monthlyBudget;
    private double totalSaved;
    private double totalSpent;
    private int monthsActive;
    private String activeLabel; // "X days" or "X months"
    private int friendCount;
    private String joinedMonth;
}
