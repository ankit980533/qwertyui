package com.xpensetrack.controller;

import com.xpensetrack.config.AuthUtil;
import com.xpensetrack.dto.ChatRequest;
import com.xpensetrack.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody ChatRequest req) {
        return ResponseEntity.ok(chatService.chat(AuthUtil.currentUserId(), req.getMessage()));
    }

    @GetMapping("/history")
    public ResponseEntity<?> history() {
        return ResponseEntity.ok(chatService.getHistory(AuthUtil.currentUserId()));
    }
}
