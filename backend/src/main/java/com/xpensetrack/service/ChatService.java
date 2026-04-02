package com.xpensetrack.service;

import com.xpensetrack.dto.ChatResponse;
import com.xpensetrack.model.ChatMessage;
import com.xpensetrack.model.ChatRole;
import com.xpensetrack.repository.ChatMessageRepository;
import com.xpensetrack.repository.ExpenseRepository;
import com.xpensetrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatMessageRepository chatRepo;
    private final UserRepository userRepo;
    private final ExpenseRepository expenseRepo;

    public ChatResponse chat(String userId, String message) {
        var userMsg = new ChatMessage();
        userMsg.setUserId(userId);
        userMsg.setRole(ChatRole.USER);
        userMsg.setContent(message);
        chatRepo.save(userMsg);

        String reply = generateReply(userId, message);

        var assistantMsg = new ChatMessage();
        assistantMsg.setUserId(userId);
        assistantMsg.setRole(ChatRole.ASSISTANT);
        assistantMsg.setContent(reply);
        chatRepo.save(assistantMsg);

        return new ChatResponse(reply, List.of("Log Expense", "Money Tips", "Show Budget"));
    }

    public List<ChatMessage> getHistory(String userId) {
        return chatRepo.findByUserIdOrderByCreatedAtAsc(userId);
    }

    private String generateReply(String userId, String message) {
        var user = userRepo.findById(userId).orElseThrow();
        var now = LocalDate.now(java.time.ZoneOffset.UTC);
        var monthStart = now.withDayOfMonth(1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
        var monthEnd = now.plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
        var expenses = expenseRepo.findByUserIdAndDateRange(userId, monthStart, monthEnd);
        double totalSpent = expenses.stream().mapToDouble(e -> e.getAmount()).sum();
        double remaining = user.getMonthlyBudget() - totalSpent;
        String msg = message.toLowerCase();

        if (msg.contains("budget") || msg.contains("show budget"))
            return String.format("Your monthly budget is ₹%.0f. Spent: ₹%.0f. Remaining: ₹%.0f.", user.getMonthlyBudget(), totalSpent, remaining);
        if (msg.contains("save") || msg.contains("money tips"))
            return String.format("Tips: 1) Track every expense. 2) Daily limit: ₹%.0f. 3) Use Piggy Bank for goals. 4) Feed your dragon!", remaining / Math.max(now.lengthOfMonth() - now.getDayOfMonth() + 1, 1));
        if (msg.contains("log") || msg.contains("expense"))
            return "Use the Add Expense button on your dashboard. Enter amount, pick a category, and optionally split with friends.";
        return "Hi! I'm your Hostel Life assistant 👋 I can help you log expenses, track savings, and give money-saving tips. What would you like to do?";
    }
}
