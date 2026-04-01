package com.xpensetrack.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "coin_packs")
public class CoinPack {
    @Id
    private String id;
    private int coins;
    private int bonusCoins = 0;
    private double priceInr;
    private String label;
}
