package com.xpensetrack.service;

import com.xpensetrack.dto.*;
import com.xpensetrack.model.*;
import com.xpensetrack.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendService {
    private final UserRepository userRepo;
    private final GroupRepository groupRepo;
    private final SplitExpenseRepository splitExpenseRepo;
    private final FriendRequestRepository friendRequestRepo;

    public FriendsOverviewResponse getOverview(String userId) {
        Map<String, Double> balanceMap = new HashMap<>();
        Map<String, List<TransactionItem>> txMap = new HashMap<>();

        splitExpenseRepo.findByPaidByUserId(userId).forEach(exp ->
            exp.getSplits().stream().filter(s -> !s.getUserId().equals(userId)).forEach(s -> {
                balanceMap.merge(s.getUserId(), s.getAmount(), Double::sum);
                txMap.computeIfAbsent(s.getUserId(), k -> new ArrayList<>())
                    .add(new TransactionItem(exp.getDescription(), s.getAmount(), s.isSettled() ? "Settled" : "Pending"));
            }));

        splitExpenseRepo.findBySplitsUserId(userId).stream()
            .filter(exp -> !exp.getPaidByUserId().equals(userId)).forEach(exp ->
                exp.getSplits().stream().filter(s -> s.getUserId().equals(userId)).forEach(s -> {
                    balanceMap.merge(exp.getPaidByUserId(), -s.getAmount(), Double::sum);
                    txMap.computeIfAbsent(exp.getPaidByUserId(), k -> new ArrayList<>())
                        .add(new TransactionItem(exp.getDescription(), -s.getAmount(), s.isSettled() ? "Settled" : "Pending"));
                }));

        var balances = balanceMap.entrySet().stream().map(e -> {
            var friend = userRepo.findById(e.getKey()).orElse(null);
            String name = friend != null ? friend.getFullName() : "Unknown";
            String avatar = friend != null ? friend.getAvatarUrl() : null;
            double amt = e.getValue();
            String label = amt > 0 ? "Owes you ₹" + (int) amt : "You owe ₹" + (int) -amt;
            return new FriendBalanceItem(e.getKey(), name, avatar, amt, label, txMap.getOrDefault(e.getKey(), List.of()));
        }).toList();

        double youOwe = balances.stream().filter(b -> b.getAmount() < 0).mapToDouble(b -> -b.getAmount()).sum();
        double toReceive = balances.stream().filter(b -> b.getAmount() > 0).mapToDouble(FriendBalanceItem::getAmount).sum();
        return new FriendsOverviewResponse(youOwe, toReceive, balances);
    }

    public String sendFriendRequest(String userId, String toUserId) {
        if (friendRequestRepo.findByFromUserIdAndToUserId(userId, toUserId).isPresent())
            throw new IllegalArgumentException("Friend request already sent");
        var req = new FriendRequest();
        req.setFromUserId(userId);
        req.setToUserId(toUserId);
        friendRequestRepo.save(req);
        return "Friend request sent";
    }

    public List<FriendRequestResponse> getPendingRequests(String userId) {
        var currentUser = userRepo.findById(userId).orElseThrow();
        return friendRequestRepo.findByToUserIdAndStatus(userId, FriendRequestStatus.PENDING).stream()
            .map(req -> {
                var sender = userRepo.findById(req.getFromUserId()).orElse(null);
                if (sender == null) return null;
                int mutual = (int) sender.getFriendIds().stream().filter(currentUser.getFriendIds()::contains).count();
                return new FriendRequestResponse(req.getId(), sender.getId(), sender.getFullName(),
                    sender.getDisplayId(), sender.getHostel(), sender.getAvatarUrl(), mutual);
            }).filter(Objects::nonNull).toList();
    }

    public String respondToRequest(String userId, String requestId, boolean accept) {
        var req = friendRequestRepo.findById(requestId).orElseThrow();
        if (!req.getToUserId().equals(userId)) throw new IllegalArgumentException("Not your request");
        if (accept) {
            req.setStatus(FriendRequestStatus.ACCEPTED);
            friendRequestRepo.save(req);
            var user = userRepo.findById(userId).orElseThrow();
            var sender = userRepo.findById(req.getFromUserId()).orElseThrow();
            user.getFriendIds().add(sender.getId());
            sender.getFriendIds().add(userId);
            userRepo.save(user);
            userRepo.save(sender);
            return "Friend request accepted";
        } else {
            req.setStatus(FriendRequestStatus.REJECTED);
            friendRequestRepo.save(req);
            return "Friend request rejected";
        }
    }

    public List<FriendSummary> getFriends(String userId) {
        var user = userRepo.findById(userId).orElseThrow();
        return user.getFriendIds().stream().map(fid -> userRepo.findById(fid).orElse(null))
            .filter(Objects::nonNull)
            .map(f -> new FriendSummary(f.getId(), f.getFullName(), f.getDisplayId(), f.getEmail(), f.getHostel(), f.getAvatarUrl()))
            .toList();
    }

    public List<FriendSummary> searchUsers(String query) {
        return userRepo.searchByNameOrId(query).stream()
            .map(u -> new FriendSummary(u.getId(), u.getFullName(), u.getDisplayId(), u.getEmail(), u.getHostel(), u.getAvatarUrl()))
            .toList();
    }

    public void settleUp(String userId, String withUserId) {
        for (var payer : List.of(userId, withUserId)) {
            String other = payer.equals(userId) ? withUserId : userId;
            splitExpenseRepo.findByPaidByUserId(payer).forEach(exp -> {
                boolean changed = false;
                for (var split : exp.getSplits()) {
                    if (split.getUserId().equals(other) && !split.isSettled()) {
                        split.setSettled(true);
                        changed = true;
                    }
                }
                if (changed) splitExpenseRepo.save(exp);
            });
        }
    }
}
