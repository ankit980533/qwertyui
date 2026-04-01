package com.xpensetrack.dto;

import com.xpensetrack.model.PaymentMethod;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {
    private String id;
    private String toUserName;
    private double amount;
    private PaymentMethod method;
    private String status;
    private double cashbackPercent;
    private double cashbackAmount;
    private boolean secure;
}
