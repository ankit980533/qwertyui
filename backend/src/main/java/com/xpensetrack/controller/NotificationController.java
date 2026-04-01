package com.xpensetrack.controller;

import com.xpensetrack.config.AuthUtil;
import com.xpensetrack.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(notificationService.getAll(AuthUtil.currentUserId()));
    }

    @GetMapping("/unread")
    public ResponseEntity<?> getUnread() {
        return ResponseEntity.ok(notificationService.getUnread(AuthUtil.currentUserId()));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable String id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok("Marked as read");
    }
}
