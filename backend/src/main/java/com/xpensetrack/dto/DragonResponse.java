package com.xpensetrack.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DragonResponse {
    private String name;
    private int level;
    private int happiness;
    private int experience;
    private int coinsToNextLevel;
    private String levelUpMessage;
    private List<String> accessories;
    private String activeSkin;
    private int userCoins;
    private int feedCost;
    private int shopMinCost;
}
