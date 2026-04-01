package com.xpensetrack.service;

import com.xpensetrack.dto.*;
import com.xpensetrack.model.PiggyBank;
import com.xpensetrack.repository.PiggyBankRepository;
import com.xpensetrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class PiggyBankService {
    private final PiggyBankRepository piggyBankRepo;
    private final UserRepository userRepo;

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
        var goals = piggyBankRepo.findByUserId(userId);
        double monthlySavings = goals.stream().mapToDouble(PiggyBank::getSavedAmount).sum();
        double target = Math.max(goals.stream().mapToDouble(PiggyBank::getTargetAmount).sum(), 100000);
        double pct = target > 0 ? (monthlySavings / target) * 100 : 0;
        var recent = goals.stream().sorted(Comparator.comparing(PiggyBank::getCreatedAt).reversed())
                .map(this::toGoalResponse).toList();
        return new PiggyBankOverviewResponse(monthlySavings, target, pct, recent);
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

    private PiggyBankGoalResponse toGoalResponse(PiggyBank pb) {
        return PiggyBankGoalResponse.builder()
                .id(pb.getId()).goalName(pb.getGoalName()).targetAmount(pb.getTargetAmount())
                .savedAmount(pb.getSavedAmount()).deadline(pb.getDeadline())
                .progressPercent(pb.getProgressPercent()).dailySavingNeeded(pb.getDailySavingNeeded())
                .imageUrl(pb.getImageUrl()).build();
    }
}
