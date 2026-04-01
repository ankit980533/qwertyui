package com.xpensetrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CoinPackResponse {
    private String id;
    private int coins;
    private int totalCoins;
    private double priceInr;
    private String label;
}
