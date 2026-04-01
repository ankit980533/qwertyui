package com.xpensetrack.controller;

import com.xpensetrack.config.AuthUtil;
import com.xpensetrack.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {
    private final FriendService friendService;

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

    @PutMapping("/requests/{id}")
    public ResponseEntity<?> respond(@PathVariable String id, @RequestParam boolean accept) {
        return ResponseEntity.ok(friendService.respondToRequest(AuthUtil.currentUserId(), id, accept));
    }

    @PostMapping("/settle")
    public ResponseEntity<?> settle(@RequestBody Map<String, String> body) {
        friendService.settleUp(AuthUtil.currentUserId(), body.get("withUserId"));
        return ResponseEntity.ok("Settled up");
    }
}
