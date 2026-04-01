package com.xpensetrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class FriendBalanceItem {
    private String userId;
    private String fullName;
    private String avatarUrl;
    private double amount;
    private String label;
    private List<TransactionItem> transactions;
}
