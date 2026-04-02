package com.xpensetrack.controller;

import com.xpensetrack.config.AuthUtil;
import com.xpensetrack.service.PiggyBankService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/piggybank")
@RequiredArgsConstructor
public class PiggyBankController {
    private final PiggyBankService piggyBankService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(piggyBankService.create(AuthUtil.currentUserId(),
                (String) body.get("goalName"),
                ((Number) body.get("targetAmount")).doubleValue(),
                LocalDate.parse((String) body.get("deadline")),
                (String) body.get("imageUrl")));
    }

    @GetMapping
    public ResponseEntity<?> overview() {
        return ResponseEntity.ok(piggyBankService.getOverview(AuthUtil.currentUserId()));
    }

    @PostMapping("/{id}/save")
    public ResponseEntity<?> addSavings(@PathVariable String id, @RequestBody Map<String, Double> body) {
        return ResponseEntity.ok(piggyBankService.addSavings(AuthUtil.currentUserId(), id, body.get("amount")));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> markComplete(@PathVariable String id) {
        return ResponseEntity.ok(piggyBankService.markComplete(AuthUtil.currentUserId(), id));
    }
}
