package com.xpensetrack.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "shop_items")
public class ShopItem {
    @Id
    private String id;
    private String name;
    private String description;
    private ShopCategory category;
    private int price;
    private int happinessBoost = 0;
    private int experienceBoost = 0;
    private String imageUrl;
}
