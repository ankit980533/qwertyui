package com.xpensetrack.dto;

import com.xpensetrack.model.ShopCategory;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShopItemResponse {
    private String id;
    private String name;
    private String description;
    private ShopCategory category;
    private int price;
    private int happinessBoost;
    private int experienceBoost;
    private String imageUrl;
    private boolean owned;
}
