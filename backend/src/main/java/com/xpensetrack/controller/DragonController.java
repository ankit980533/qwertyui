package com.xpensetrack.controller;

import com.xpensetrack.config.AuthUtil;
import com.xpensetrack.service.DragonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/dragon")
@RequiredArgsConstructor
public class DragonController {
    private final DragonService dragonService;

    @GetMapping
    public ResponseEntity<?> get() {
        return ResponseEntity.ok(dragonService.getDragon(AuthUtil.currentUserId()));
    }

    @PostMapping("/feed")
    public ResponseEntity<?> feed(@RequestBody Map<String, Integer> body) {
        return ResponseEntity.ok(dragonService.feedDragon(AuthUtil.currentUserId(), body.get("coins")));
    }

    @GetMapping("/shop")
    public ResponseEntity<?> shop() {
        return ResponseEntity.ok(dragonService.getShop(AuthUtil.currentUserId()));
    }

    @PostMapping("/shop/buy")
    public ResponseEntity<?> buy(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(dragonService.purchaseItem(AuthUtil.currentUserId(), body.get("itemId")));
    }

    @PostMapping("/shop/coins")
    public ResponseEntity<?> buyCoins(@RequestBody Map<String, String> body) {
        int coins = dragonService.purchaseCoinPack(AuthUtil.currentUserId(), body.get("packId"));
        return ResponseEntity.ok(Map.of("coins", coins));
    }
}
