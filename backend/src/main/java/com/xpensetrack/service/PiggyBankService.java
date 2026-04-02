package com.xpensetrack.service;

import com.xpensetrack.dto.*;
import com.xpensetrack.model.Expense;
import com.xpensetrack.model.PiggyBank;
import com.xpensetrack.repository.ExpenseRepository;
import com.xpensetrack.repository.PiggyBankRepository;
import com.xpensetrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class PiggyBankService {
    private final PiggyBankRepository piggyBankRepo;
    private final UserRepository userRepo;
    private final ExpenseRepository expenseRepo;

    public PiggyBankGoalResponse create(String userId, String goalName, double targetAmount, LocalDate deadline, String imageUrl) {
        var pb = new PiggyBank();
        pb.setUserId(userId);
        pb.setGoalName(goalName);
        pb.setTargetAmount(targetAmount);
        pb.setDeadline(deadline);
        pb.setImageUrl(imageUrl);
        pb = piggyBankRepo.save(pb);
        return toGoalResponse(pb);
    }

    public PiggyBankOverviewResponse getOverview(String userId) {
        var user = userRepo.findById(userId).orElseThrow();
        var goals = piggyBankRepo.findByUserId(userId);

        // Calculate current month savings (from day 1 to today)
        var now = LocalDate.now(ZoneOffset.UTC);
        var monthStart = now.withDayOfMonth(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        var todayEnd = now.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        double currentMonthSpent = expenseRepo.findByUserIdAndDateRange(userId, monthStart, todayEnd)
                .stream().mapToDouble(Expense::getAmount).sum();
        
        // Calculate daily budget and current month savings
        int daysInMonth = now.lengthOfMonth();
        int daysPassed = now.getDayOfMonth();
        double dailyBudget = user.getMonthlyBudget() / daysInMonth;
        double currentMonthBudgetSoFar = dailyBudget * daysPassed;
        double currentMonthSavings = Math.max(currentMonthBudgetSoFar - currentMonthSpent, 0);
        
        // Calculate ONLY completed previous months' savings (not current month)
        var accountCreated = user.getCreatedAt() != null ? user.getCreatedAt() : Instant.now();
        var firstMonth = accountCreated.atZone(ZoneOffset.UTC).toLocalDate().withDayOfMonth(1);
        
        double previousMonthsSavings = 0;
        var currentMonth = firstMonth;
        var thisMonthStart = now.withDayOfMonth(1);
        
        // Loop through all months BEFORE current month
        while (currentMonth.isBefore(thisMonthStart)) {
            var mStart = currentMonth.atStartOfDay(ZoneOffset.UTC).toInstant();
            var mEnd = currentMonth.plusMonths(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            double monthSpent = expenseRepo.findByUserIdAndDateRange(userId, mStart, mEnd)
                    .stream().mapToDouble(Expense::getAmount).sum();
            previousMonthsSavings += Math.max(user.getMonthlyBudget() - monthSpent, 0);
            currentMonth = currentMonth.plusMonths(1);
        }
        
        // Total savings = current month (partial) + completed previous months only
        double totalSavings = currentMonthSavings + previousMonthsSavings;
        
        // Auto-distribute current month savings to goals (earliest deadline first)
        if (currentMonthSavings > 0 && !goals.isEmpty()) {
            double remainingSavings = currentMonthSavings;
            
            // Sort goals by deadline (earliest first)
            var sortedGoals = goals.stream()
                .filter(g -> g.getSavedAmount() < g.getTargetAmount()) // Only incomplete goals
                .sorted((a, b) -> a.getDeadline().compareTo(b.getDeadline()))
                .toList();
            
            for (var goal : sortedGoals) {
                if (remainingSavings <= 0) break;
                
                double needed = goal.getTargetAmount() - goal.getSavedAmount();
                double toAdd = Math.min(needed, remainingSavings);
                
                goal.setSavedAmount(goal.getSavedAmount() + toAdd);
                piggyBankRepo.save(goal);
                remainingSavings -= toAdd;
            }
        }
        
        double goalsTotal = goals.stream().mapToDouble(PiggyBank::getTargetAmount).sum();
        double target = goalsTotal > 0 ? goalsTotal : user.getMonthlyBudget();
        double pct = target > 0 ? (totalSavings / target) * 100 : 0;

        var recent = goals.stream().sorted(Comparator.comparing(PiggyBank::getCreatedAt).reversed())
                .map(this::toGoalResponse).toList();

        return new PiggyBankOverviewResponse(totalSavings, target, pct, recent);
    }

    public PiggyBankGoalResponse addSavings(String userId, String piggyBankId, double amount) {
        var pb = piggyBankRepo.findById(piggyBankId).orElseThrow();
        if (!pb.getUserId().equals(userId)) throw new IllegalArgumentException("Not your piggy bank");
        pb.setSavedAmount(pb.getSavedAmount() + amount);
        pb = piggyBankRepo.save(pb);
        int coinsEarned = Math.max((int) (amount / 100), 1);
        var user = userRepo.findById(userId).orElseThrow();
        user.setCoins(user.getCoins() + coinsEarned);
        userRepo.save(user);
        return toGoalResponse(pb);
    }

    public PiggyBankGoalResponse markComplete(String userId, String piggyBankId) {
        var pb = piggyBankRepo.findById(piggyBankId).orElseThrow();
        if (!pb.getUserId().equals(userId)) throw new IllegalArgumentException("Not your piggy bank");
        
        // Calculate how much is needed to complete the goal
        double needed = pb.getTargetAmount() - pb.getSavedAmount();
        
        // Get current total savings
        var user = userRepo.findById(userId).orElseThrow();
        var now = LocalDate.now(ZoneOffset.UTC);
        var monthStart = now.withDayOfMonth(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        var todayEnd = now.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        double currentMonthSpent = expenseRepo.findByUserIdAndDateRange(userId, monthStart, todayEnd)
                .stream().mapToDouble(Expense::getAmount).sum();
        
        int daysInMonth = now.lengthOfMonth();
        int daysPassed = now.getDayOfMonth();
        double dailyBudget = user.getMonthlyBudget() / daysInMonth;
        double currentMonthBudgetSoFar = dailyBudget * daysPassed;
        double currentMonthSavings = Math.max(currentMonthBudgetSoFar - currentMonthSpent, 0);
        
        // Calculate previous months savings
        var accountCreated = user.getCreatedAt() != null ? user.getCreatedAt() : Instant.now();
        var firstMonth = accountCreated.atZone(ZoneOffset.UTC).toLocalDate().withDayOfMonth(1);
        double previousMonthsSavings = 0;
        var currentMonth = firstMonth;
        var thisMonthStart = now.withDayOfMonth(1);
        
        while (currentMonth.isBefore(thisMonthStart)) {
            var mStart = currentMonth.atStartOfDay(ZoneOffset.UTC).toInstant();
            var mEnd = currentMonth.plusMonths(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            double monthSpent = expenseRepo.findByUserIdAndDateRange(userId, mStart, mEnd)
                    .stream().mapToDouble(Expense::getAmount).sum();
            previousMonthsSavings += Math.max(user.getMonthlyBudget() - monthSpent, 0);
            currentMonth = currentMonth.plusMonths(1);
        }
        
        double totalSavings = currentMonthSavings + previousMonthsSavings;
        
        // Check if user has enough savings
        if (needed > totalSavings) {
            throw new IllegalArgumentException("Insufficient savings. You need ₹" + (int)needed + " but only have ₹" + (int)totalSavings + " saved.");
        }
        
        // Mark as complete and deduct from savings (this will be reflected in next overview call)
        pb.setSavedAmount(pb.getTargetAmount());
        pb = piggyBankRepo.save(pb);
        
        // Award bonus coins for completing goal
        user.setCoins(user.getCoins() + 50);
        userRepo.save(user);
        
        return toGoalResponse(pb);
    }

    private PiggyBankGoalResponse toGoalResponse(PiggyBank pb) {
        return PiggyBankGoalResponse.builder()
                .id(pb.getId()).goalName(pb.getGoalName()).targetAmount(pb.getTargetAmount())
                .savedAmount(pb.getSavedAmount()).deadline(pb.getDeadline())
                .progressPercent(pb.getProgressPercent()).dailySavingNeeded(pb.getDailySavingNeeded())
                .imageUrl(pb.getImageUrl()).build();
    }
}
