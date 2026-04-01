package com.xpensetrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class ShopResponse {
    private int userCoins;
    private List<ShopItemResponse> food;
    private List<ShopItemResponse> addins;
    private List<ShopItemResponse> skins;
    private List<CoinPackResponse> coinPacks;
}
