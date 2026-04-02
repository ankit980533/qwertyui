package com.xpensetrack.service;

import com.xpensetrack.config.JwtUtil;
import com.xpensetrack.dto.*;
import com.xpensetrack.model.Dragon;
import com.xpensetrack.model.User;
import com.xpensetrack.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
        
        // Calculate total savings same as PiggyBankService
        var now = LocalDate.now();
        var monthStart = now.withDayOfMonth(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        var todayEnd = now.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        double currentMonthSpent = expenseRepo.findByUserIdAndDateRange(userId, monthStart, todayEnd)
                .stream().mapToDouble(e -> e.getAmount()).sum();
        
        // Calculate daily budget and current month savings
        int daysInMonth = now.lengthOfMonth();
        int daysPassed = now.getDayOfMonth();
        double dailyBudget = user.getMonthlyBudget() / daysInMonth;
        double currentMonthBudgetSoFar = dailyBudget * daysPassed;
        double currentMonthSavings = Math.max(currentMonthBudgetSoFar - currentMonthSpent, 0);
        
        // Calculate ONLY completed previous months' savings
        var accountCreated = user.getCreatedAt() != null ? user.getCreatedAt() : Instant.now();
        var firstMonth = accountCreated.atZone(ZoneOffset.UTC).toLocalDate().withDayOfMonth(1);
        double previousMonthsSavings = 0;
        var currentMonth = firstMonth;
        var thisMonthStart = now.withDayOfMonth(1);
        
        while (currentMonth.isBefore(thisMonthStart)) {
            var mStart = currentMonth.atStartOfDay(ZoneOffset.UTC).toInstant();
            var mEnd = currentMonth.plusMonths(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            double monthSpent = expenseRepo.findByUserIdAndDateRange(userId, mStart, mEnd)
                    .stream().mapToDouble(e -> e.getAmount()).sum();
            previousMonthsSavings += Math.max(user.getMonthlyBudget() - monthSpent, 0);
            currentMonth = currentMonth.plusMonths(1);
        }
        
        // Total savings = current month (partial) + completed previous months
        double totalSaved = currentMonthSavings + previousMonthsSavings;
        
        var createdDate = user.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
        
        // Calculate completed months (only count full months)
        var firstDayOfCreatedMonth = createdDate.withDayOfMonth(1);
        var firstDayOfCurrentMonth = now.withDayOfMonth(1);
        int completedMonths = (int) ChronoUnit.MONTHS.between(firstDayOfCreatedMonth, firstDayOfCurrentMonth);
        
        // Calculate days if less than 1 complete month
        long daysSinceCreation = ChronoUnit.DAYS.between(createdDate, now);
        
        String activeLabel;
        int monthsActive;
        if (completedMonths == 0) {
            // Less than 1 complete month - show days
            activeLabel = daysSinceCreation + (daysSinceCreation == 1 ? " day" : " days");
            monthsActive = 0;
        } else {
            // 1 or more complete months
            activeLabel = completedMonths + (completedMonths == 1 ? " month" : " months");
            monthsActive = completedMonths;
        }

        return UserProfileResponse.builder()
                .id(user.getId()).displayId(user.getDisplayId()).fullName(user.getFullName())
                .email(user.getEmail()).phoneNumber(user.getPhoneNumber()).address(user.getAddress())
                .hostel(user.getHostel()).avatarUrl(user.getAvatarUrl()).coins(user.getCoins())
                .currentBalance(user.getCurrentBalance()).monthlyBudget(user.getMonthlyBudget())
                .totalSaved(totalSaved).totalSpent(totalSpent).monthsActive(monthsActive)
                .activeLabel(activeLabel)
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
