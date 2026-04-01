package com.xpensetrack.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String phoneNumber;
    private String address;
    private String hostel;
    private String avatarUrl;
    private Double monthlyBudget;
}
