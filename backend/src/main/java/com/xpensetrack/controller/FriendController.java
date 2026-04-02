package com.xpensetrack.controller;

import com.xpensetrack.config.AuthUtil;
import com.xpensetrack.model.Group;
import com.xpensetrack.model.Split;
import com.xpensetrack.model.SplitExpense;
import com.xpensetrack.repository.GroupRepository;
import com.xpensetrack.repository.SplitExpenseRepository;
import com.xpensetrack.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {
    private final FriendService friendService;
    private final GroupRepository groupRepo;
    private final SplitExpenseRepository splitExpenseRepo;

    @GetMapping("/overview")
    public ResponseEntity<?> overview() {
        return ResponseEntity.ok(friendService.getOverview(AuthUtil.currentUserId()));
    }

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(friendService.getFriends(AuthUtil.currentUserId()));
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String query) {
        return ResponseEntity.ok(friendService.searchUsers(query));
    }

    @PostMapping("/request")
    public ResponseEntity<?> sendRequest(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(friendService.sendFriendRequest(AuthUtil.currentUserId(), body.get("toUserId")));
    }

    @GetMapping("/requests")
    public ResponseEntity<?> pendingRequests() {
        return ResponseEntity.ok(friendService.getPendingRequests(AuthUtil.currentUserId()));
    }

    @GetMapping("/requests/sent")
    public ResponseEntity<?> sentRequests() {
        return ResponseEntity.ok(friendService.getSentRequests(AuthUtil.currentUserId()));
    }

    @DeleteMapping("/requests/{id}")
    public ResponseEntity<?> revokeRequest(@PathVariable String id) {
        friendService.revokeRequest(AuthUtil.currentUserId(), id);
        return ResponseEntity.ok("Request revoked");
    }

    @PutMapping("/requests/{id}")
    public ResponseEntity<?> respond(@PathVariable String id, @RequestParam boolean accept) {
        return ResponseEntity.ok(friendService.respondToRequest(AuthUtil.currentUserId(), id, accept));
    }

    @PostMapping("/settle")
    public ResponseEntity<?> settle(@RequestBody Map<String, String> body) {
        friendService.settleUp(AuthUtil.currentUserId(), body.get("withUserId"));
        return ResponseEntity.ok("Settled up");
    }

    // --- Groups ---
    @PostMapping("/groups")
    public ResponseEntity<?> createGroup(@RequestBody Map<String, Object> body) {
        var group = new Group();
        group.setName((String) body.get("name"));
        @SuppressWarnings("unchecked")
        var memberIds = (List<String>) body.get("memberIds");
        memberIds.add(AuthUtil.currentUserId());
        group.setMemberIds(memberIds.stream().distinct().collect(Collectors.toList()));
        group.setCreatedBy(AuthUtil.currentUserId());
        return ResponseEntity.ok(groupRepo.save(group));
    }

    @GetMapping("/groups")
    public ResponseEntity<?> getGroups() {
        return ResponseEntity.ok(groupRepo.findByMemberIdsContaining(AuthUtil.currentUserId()));
    }

    // --- Split Expense ---
    @PostMapping("/split")
    public ResponseEntity<?> splitExpense(@RequestBody Map<String, Object> body) {
        var expense = new SplitExpense();
        expense.setPaidByUserId(AuthUtil.currentUserId());
        expense.setDescription((String) body.get("description"));
        expense.setTotalAmount(((Number) body.get("totalAmount")).doubleValue());
        expense.setGroupId((String) body.get("groupId"));

        @SuppressWarnings("unchecked")
        var splitAmong = (List<Map<String, Object>>) body.get("splitAmong");
        double equalAmount = expense.getTotalAmount() / splitAmong.size();

        var splits = splitAmong.stream().map(s -> {
            var split = new Split();
            split.setUserId((String) s.get("userId"));
            split.setAmount(s.get("amount") != null ? ((Number) s.get("amount")).doubleValue() : equalAmount);
            return split;
        }).collect(Collectors.toList());

        expense.setSplits(splits);
        return ResponseEntity.ok(splitExpenseRepo.save(expense));
    }
}
