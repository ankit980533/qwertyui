package com.xpensetrack.service;

import com.xpensetrack.config.JwtUtil;
import com.xpensetrack.dto.*;
import com.xpensetrack.model.Dragon;
import com.xpensetrack.model.User;
import com.xpensetrack.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepo;
    private final DragonRepository dragonRepo;
    private final ExpenseRepository expenseRepo;
    private final PiggyBankRepository piggyBankRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse signup(SignupRequest req) {
        if (!req.getPassword().equals(req.getConfirmPassword()))
            throw new IllegalArgumentException("Passwords do not match");
        if (!req.isTermsAccepted())
            throw new IllegalArgumentException("You must accept the Terms and Conditions");
        if (userRepo.existsByEmail(req.getEmail()))
            throw new IllegalArgumentException("Email already registered");

        var user = new User();
        user.setFullName(req.getFullName());
        user.setPhoneNumber(req.getPhoneNumber());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setTermsAccepted(true);
        user.setMonthlyBudget(req.getMonthlyBudget() > 0 ? req.getMonthlyBudget() : 5000);
        user = userRepo.save(user);

        var dragon = new Dragon();
        dragon.setUserId(user.getId());
        dragonRepo.save(dragon);

        return new AuthResponse(jwtUtil.generateToken(user.getId(), user.getEmail()),
                user.getId(), user.getFullName(), user.getEmail());
    }

    public AuthResponse login(LoginRequest req) {
        var user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword()))
            throw new IllegalArgumentException("Invalid credentials");
        return new AuthResponse(jwtUtil.generateToken(user.getId(), user.getEmail()),
                user.getId(), user.getFullName(), user.getEmail());
    }

    public UserProfileResponse getProfile(String userId) {
        var user = userRepo.findById(userId).orElseThrow();
        double totalSpent = expenseRepo.findByUserIdOrderByDateDesc(userId).stream().mapToDouble(e -> e.getAmount()).sum();
        double totalSaved = piggyBankRepo.findByUserId(userId).stream().mapToDouble(p -> p.getSavedAmount()).sum();
        var created = user.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate().withDayOfMonth(1);
        int monthsActive = (int) ChronoUnit.MONTHS.between(created, LocalDate.now().withDayOfMonth(1)) + 1;

        return UserProfileResponse.builder()
                .id(user.getId()).displayId(user.getDisplayId()).fullName(user.getFullName())
                .email(user.getEmail()).phoneNumber(user.getPhoneNumber()).address(user.getAddress())
                .hostel(user.getHostel()).avatarUrl(user.getAvatarUrl()).coins(user.getCoins())
                .currentBalance(user.getCurrentBalance()).monthlyBudget(user.getMonthlyBudget())
                .totalSaved(totalSaved).totalSpent(totalSpent).monthsActive(monthsActive)
                .friendCount(user.getFriendIds().size()).joinedMonth(user.getJoinedMonth())
                .build();
    }

    public UserProfileResponse updateProfile(String userId, UpdateProfileRequest req) {
        var user = userRepo.findById(userId).orElseThrow();
        if (req.getFullName() != null) user.setFullName(req.getFullName());
        if (req.getPhoneNumber() != null) user.setPhoneNumber(req.getPhoneNumber());
        if (req.getAddress() != null) user.setAddress(req.getAddress());
        if (req.getHostel() != null) user.setHostel(req.getHostel());
        if (req.getAvatarUrl() != null) user.setAvatarUrl(req.getAvatarUrl());
        if (req.getMonthlyBudget() != null) user.setMonthlyBudget(req.getMonthlyBudget());
        userRepo.save(user);
        return getProfile(userId);
    }

    public UserProfileResponse updateBalance(String userId, double balance) {
        var user = userRepo.findById(userId).orElseThrow();
        user.setCurrentBalance(balance);
        userRepo.save(user);
        return getProfile(userId);
    }
}
