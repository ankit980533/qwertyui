package com.xpensetrack.repository;

import com.xpensetrack.model.ShopCategory;
import com.xpensetrack.model.ShopItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ShopItemRepository extends MongoRepository<ShopItem, String> {
    List<ShopItem> findByCategory(ShopCategory category);
}
