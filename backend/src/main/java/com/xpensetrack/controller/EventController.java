package com.xpensetrack.controller;

import com.xpensetrack.config.AuthUtil;
import com.xpensetrack.model.UpcomingEvent;
import com.xpensetrack.repository.UpcomingEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final UpcomingEventRepository eventRepo;

    @PostMapping
    public ResponseEntity<?> add(@RequestBody Map<String, Object> body) {
        var event = new UpcomingEvent();
        event.setUserId(AuthUtil.currentUserId());
        event.setTitle((String) body.get("title"));
        event.setAmount(((Number) body.get("amount")).doubleValue());
        event.setDueDate(LocalDate.parse((String) body.get("dueDate")));
        event.setRecurring(Boolean.TRUE.equals(body.get("recurring")));
        return ResponseEntity.ok(eventRepo.save(event));
    }

    @GetMapping
    public ResponseEntity<?> upcoming() {
        return ResponseEntity.ok(eventRepo.findByUserIdAndDueDateGreaterThanEqualOrderByDueDateAsc(
                AuthUtil.currentUserId(), LocalDate.now()));
    }

    @PutMapping("/{id}/paid")
    public ResponseEntity<?> markPaid(@PathVariable String id) {
        var event = eventRepo.findById(id).orElseThrow();
        event.setPaid(true);
        return ResponseEntity.ok(eventRepo.save(event));
    }
}
