package com.xpensetrack.controller;

import com.xpensetrack.config.AuthUtil;
import com.xpensetrack.model.PaymentMethod;
import com.xpensetrack.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<?> pay(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(paymentService.initiatePayment(AuthUtil.currentUserId(),
                (String) body.get("toUserId"),
                ((Number) body.get("amount")).doubleValue(),
                PaymentMethod.valueOf((String) body.get("method"))));
    }

    @GetMapping("/history")
    public ResponseEntity<?> history() {
        return ResponseEntity.ok(paymentService.getHistory(AuthUtil.currentUserId()));
    }
}
