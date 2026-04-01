package com.xpensetrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionItem {
    private String description;
    private double amount;
    private String status;
}
