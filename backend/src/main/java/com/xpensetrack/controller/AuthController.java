package com.xpensetrack.controller;

import com.xpensetrack.config.AuthUtil;
import com.xpensetrack.dto.*;
import com.xpensetrack.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/auth/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest req) {
        return ResponseEntity.ok(authService.signup(req));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        return ResponseEntity.ok(authService.getProfile(AuthUtil.currentUserId()));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(authService.updateProfile(AuthUtil.currentUserId(), req));
    }

    @PutMapping("/profile/balance")
    public ResponseEntity<?> updateBalance(@RequestBody java.util.Map<String, Double> body) {
        return ResponseEntity.ok(authService.updateBalance(AuthUtil.currentUserId(), body.get("currentBalance")));
    }
}
