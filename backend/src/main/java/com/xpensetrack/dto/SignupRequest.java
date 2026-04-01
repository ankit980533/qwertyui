package com.xpensetrack.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {
    @NotBlank private String fullName;
    @NotBlank private String phoneNumber;
    @Email private String email;
    @Size(min = 6) private String password;
    @Size(min = 6) private String confirmPassword;
    private boolean termsAccepted;
    private double monthlyBudget = 0;
}
