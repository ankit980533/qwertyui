package com.xpensetrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class FriendsOverviewResponse {
    private double youOwe;
    private double toReceive;
    private List<FriendBalanceItem> friendBalances;
}
