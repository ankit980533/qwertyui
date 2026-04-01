package com.xpensetrack.service;

import com.xpensetrack.dto.*;
import com.xpensetrack.model.ShopCategory;
import com.xpensetrack.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DragonService {
    private final DragonRepository dragonRepo;
    private final ShopItemRepository shopItemRepo;
    private final CoinPackRepository coinPackRepo;
    private final UserRepository userRepo;

    public DragonResponse getDragon(String userId) {
        var dragon = dragonRepo.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("Dragon not found"));
        var user = userRepo.findById(userId).orElseThrow();
        return DragonResponse.builder()
                .name(dragon.getName()).level(dragon.getLevel()).happiness(dragon.getHappiness())
                .experience(dragon.getExperience()).coinsToNextLevel(dragon.getCoinsToNextLevel())
                .levelUpMessage("Unlock the level 10 and unleash new pet")
                .accessories(dragon.getOwnedItemIds()).activeSkin(dragon.getActiveSkinId())
                .userCoins(user.getCoins()).feedCost(100).shopMinCost(300).build();
    }

    public DragonResponse feedDragon(String userId, int coins) {
        var user = userRepo.findById(userId).orElseThrow();
        if (user.getCoins() < coins) throw new IllegalArgumentException("Not enough coins");
        var dragon = dragonRepo.findByUserId(userId).orElseThrow();

        int expGain = coins * 10;
        int happinessGain = Math.min(coins * 2, 100 - dragon.getHappiness());
        int newExp = dragon.getExperience() + expGain;
        int newLevel = dragon.getLevel();
        while (newExp >= newLevel * 3000) { newExp -= newLevel * 3000; newLevel++; }

        dragon.setExperience(newExp);
        dragon.setLevel(newLevel);
        dragon.setHappiness(Math.min(dragon.getHappiness() + happinessGain, 100));
        dragonRepo.save(dragon);
        user.setCoins(user.getCoins() - coins);
        userRepo.save(user);
        return getDragon(userId);
    }

    public ShopResponse getShop(String userId) {
        var user = userRepo.findById(userId).orElseThrow();
        var dragon = dragonRepo.findByUserId(userId).orElse(null);

        var food = shopItemRepo.findByCategory(ShopCategory.FOOD).stream().map(i -> mapItem(i, dragon)).toList();
        var addins = shopItemRepo.findByCategory(ShopCategory.ADDINS).stream().map(i -> mapItem(i, dragon)).toList();
        var skins = shopItemRepo.findByCategory(ShopCategory.SKINS).stream().map(i -> mapItem(i, dragon)).toList();
        var packs = coinPackRepo.findAll().stream()
                .map(p -> new CoinPackResponse(p.getId(), p.getCoins(), p.getCoins() + p.getBonusCoins(), p.getPriceInr(), p.getLabel())).toList();

        return new ShopResponse(user.getCoins(), food, addins, skins, packs);
    }

    public DragonResponse purchaseItem(String userId, String itemId) {
        var user = userRepo.findById(userId).orElseThrow();
        var item = shopItemRepo.findById(itemId).orElseThrow();
        if (user.getCoins() < item.getPrice()) throw new IllegalArgumentException("Not enough coins");
        var dragon = dragonRepo.findByUserId(userId).orElseThrow();

        if (item.getCategory() == ShopCategory.SKINS) {
            dragon.getOwnedSkinIds().add(item.getId());
            dragon.setActiveSkinId(item.getId());
        } else {
            dragon.getOwnedItemIds().add(item.getId());
            dragon.setHappiness(Math.min(dragon.getHappiness() + item.getHappinessBoost(), 100));
            dragon.setExperience(dragon.getExperience() + item.getExperienceBoost());
        }
        dragonRepo.save(dragon);
        user.setCoins(user.getCoins() - item.getPrice());
        userRepo.save(user);
        return getDragon(userId);
    }

    public int purchaseCoinPack(String userId, String packId) {
        var pack = coinPackRepo.findById(packId).orElseThrow();
        var user = userRepo.findById(userId).orElseThrow();
        int total = pack.getCoins() + pack.getBonusCoins();
        user.setCoins(user.getCoins() + total);
        userRepo.save(user);
        return user.getCoins();
    }

    private ShopItemResponse mapItem(com.xpensetrack.model.ShopItem item, com.xpensetrack.model.Dragon dragon) {
        boolean owned = dragon != null && (dragon.getOwnedItemIds().contains(item.getId()) || dragon.getOwnedSkinIds().contains(item.getId()));
        return ShopItemResponse.builder()
                .id(item.getId()).name(item.getName()).description(item.getDescription())
                .category(item.getCategory()).price(item.getPrice())
                .happinessBoost(item.getHappinessBoost()).experienceBoost(item.getExperienceBoost())
                .imageUrl(item.getImageUrl()).owned(owned).build();
    }
}
