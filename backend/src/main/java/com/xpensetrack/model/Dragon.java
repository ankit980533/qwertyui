package com.xpensetrack.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "dragons")
public class Dragon {
    @Id
    private String id;
    private String userId;
    private String name = "Baby Dragon";
    private int level = 1;
    private int happiness = 50;
    private int experience = 0;
    private String activeSkinId;
    private List<String> ownedItemIds = new ArrayList<>();
    private List<String> ownedSkinIds = new ArrayList<>();

    public int getCoinsToNextLevel() {
        return level * 3000;
    }
}
