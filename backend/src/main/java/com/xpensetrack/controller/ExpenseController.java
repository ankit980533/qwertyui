package com.xpensetrack.controller;

import com.xpensetrack.config.AuthUtil;
import com.xpensetrack.dto.AddExpenseRequest;
import com.xpensetrack.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<?> add(@Valid @RequestBody AddExpenseRequest req) {
        return ResponseEntity.ok(expenseService.addExpense(AuthUtil.currentUserId(), req));
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(expenseService.getExpenses(AuthUtil.currentUserId()));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        return ResponseEntity.ok(expenseService.getDashboard(AuthUtil.currentUserId()));
    }

    @GetMapping("/report")
    public ResponseEntity<?> report(@RequestParam String period, @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(expenseService.getReport(AuthUtil.currentUserId(), period, year, month));
    }

    @GetMapping("/calendar")
    public ResponseEntity<?> calendar(@RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(expenseService.getCalendar(AuthUtil.currentUserId(), year, month));
    }
}
