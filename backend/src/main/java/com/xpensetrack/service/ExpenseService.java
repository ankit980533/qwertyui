package com.xpensetrack.service;

import com.xpensetrack.dto.*;
import com.xpensetrack.model.Expense;
import com.xpensetrack.model.ExpenseCategory;
import com.xpensetrack.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepo;
    private final UserRepository userRepo;
    private final DragonRepository dragonRepo;
    private final NotificationRepository notificationRepo;
    private final UpcomingEventRepository upcomingEventRepo;

    public ExpenseResponse addExpense(String userId, AddExpenseRequest req) {
        var expense = new Expense();
        expense.setUserId(userId);
        expense.setAmount(req.getAmount());
        expense.setDescription(req.getDescription());
        expense.setCategory(req.getCategory());
        expense.setNote(req.getNote());
        expense.setDate(req.getDate() != null ? req.getDate() : LocalDate.now());
        expense.setSplitWithFriendIds(req.getSplitWithFriendIds());
        expense = expenseRepo.save(expense);

        var user = userRepo.findById(userId).orElseThrow();
        user.setCoins(user.getCoins() + 1);
        userRepo.save(user);

        return toResponse(expense);
    }

    public List<ExpenseResponse> getExpenses(String userId) {
        return expenseRepo.findByUserIdOrderByDateDesc(userId).stream().map(this::toResponse).toList();
    }

    public DashboardResponse getDashboard(String userId) {
        var user = userRepo.findById(userId).orElseThrow();
        var now = LocalDate.now();
        var expenses = expenseRepo.findByUserIdAndDateBetween(userId, now.withDayOfMonth(1), now);
        var dragon = dragonRepo.findByUserId(userId).orElse(null);
        long unread = notificationRepo.countByUserIdAndReadFalse(userId);

        double monthlySpent = expenses.stream().mapToDouble(Expense::getAmount).sum();
        double remaining = user.getMonthlyBudget() - monthlySpent;
        double usedPct = user.getMonthlyBudget() > 0 ? (monthlySpent / user.getMonthlyBudget()) * 100 : 0;

        Map<ExpenseCategory, Double> breakdown = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategory, Collectors.summingDouble(Expense::getAmount)));

        return DashboardResponse.builder()
                .fullName(user.getFullName()).currentBalance(user.getCurrentBalance())
                .monthlyBudget(user.getMonthlyBudget()).monthlySpent(monthlySpent)
                .remaining(Math.max(remaining, 0)).budgetUsedPercent(Math.min(usedPct, 100))
                .budgetLeftPercent(Math.max(100 - usedPct, 0)).coins(user.getCoins())
                .recentExpenses(expenses.stream().limit(5).map(this::toResponse).toList())
                .dragonLevel(dragon != null ? dragon.getLevel() : 1)
                .dragonHappiness(dragon != null ? dragon.getHappiness() : 50)
                .dragonHungry(dragon != null && dragon.getHappiness() < 30)
                .monthlyBreakdown(breakdown).unreadNotificationCount((int) unread)
                .build();
    }

    public ReportResponse getReport(String userId, String period, int year, int month) {
        var ym = YearMonth.of(year, month);
        var expenses = expenseRepo.findByUserIdAndDateBetween(userId, ym.atDay(1), ym.atEndOfMonth());
        double totalSpent = expenses.stream().mapToDouble(Expense::getAmount).sum();

        var prevYm = ym.minusMonths(1);
        var prevExpenses = expenseRepo.findByUserIdAndDateBetween(userId, prevYm.atDay(1), prevYm.atEndOfMonth());
        double prevSpent = prevExpenses.stream().mapToDouble(Expense::getAmount).sum();
        double totalSaved = Math.max(prevSpent - totalSpent, 0);
        double savedChange = prevSpent > 0 ? (totalSaved / prevSpent) * 100 : 0;

        var user = userRepo.findById(userId).orElseThrow();

        List<TrendPoint> monthlyTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            var m = ym.minusMonths(i);
            var mExp = expenseRepo.findByUserIdAndDateBetween(userId, m.atDay(1), m.atEndOfMonth());
            double s = mExp.stream().mapToDouble(Expense::getAmount).sum();
            String label = m.getMonth().name().substring(0, 3);
            label = label.charAt(0) + label.substring(1).toLowerCase();
            monthlyTrend.add(new TrendPoint(label, s, user.getMonthlyBudget(), null));
        }

        var weekField = WeekFields.of(Locale.getDefault()).weekOfMonth();
        Map<Integer, Double> weekMap = expenses.stream()
                .collect(Collectors.groupingBy(e -> e.getDate().get(weekField), Collectors.summingDouble(Expense::getAmount)));
        List<TrendPoint> weekly = new TreeMap<>(weekMap).entrySet().stream()
                .map(e -> new TrendPoint("Week " + e.getKey(), e.getValue(), null, null)).toList();

        List<TrendPoint> savingsTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            var m = ym.minusMonths(i);
            var mExp = expenseRepo.findByUserIdAndDateBetween(userId, m.atDay(1), m.atEndOfMonth());
            double s = mExp.stream().mapToDouble(Expense::getAmount).sum();
            String label = m.getMonth().name().substring(0, 3);
            label = label.charAt(0) + label.substring(1).toLowerCase();
            savingsTrend.add(new TrendPoint(label, s, null, Math.max(user.getMonthlyBudget() - s, 0)));
        }

        return ReportResponse.builder().period(period).totalSpent(totalSpent).totalSaved(totalSaved)
                .savedChangePercent(savedChange).monthlySpendingTrend(monthlyTrend)
                .weeklySpending(weekly).savingsTrend(savingsTrend).build();
    }

    public CalendarResponse getCalendar(String userId, int year, int month) {
        var ym = YearMonth.of(year, month);
        var expenses = expenseRepo.findByUserIdAndDateBetween(userId, ym.atDay(1), ym.atEndOfMonth());
        Map<LocalDate, Double> dailyTotals = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getDate, Collectors.summingDouble(Expense::getAmount)));
        double avg = dailyTotals.isEmpty() ? 0 : dailyTotals.values().stream().mapToDouble(d -> d).average().orElse(0);

        List<CalendarDayInfo> days = new ArrayList<>();
        for (int d = 1; d <= ym.lengthOfMonth(); d++) {
            var date = ym.atDay(d);
            double spent = dailyTotals.getOrDefault(date, 0.0);
            String status = spent > avg * 1.5 ? "SPENT_MOST" : (spent == 0 || spent < avg * 0.5) ? "SPENT_LEAST" : "NORMAL";
            days.add(new CalendarDayInfo(date, spent, status));
        }

        var upcoming = upcomingEventRepo.findByUserIdAndDueDateGreaterThanEqualOrderByDueDateAsc(userId, LocalDate.now())
                .stream().limit(5).map(e -> new UpcomingEventResponse(e.getId(), e.getTitle(), e.getAmount(), e.getDueDate(), e.isPaid())).toList();

        var today = expenseRepo.findByUserIdAndDate(userId, LocalDate.now()).stream().map(this::toResponse).toList();

        return new CalendarResponse(year, month, days, upcoming, today);
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(e.getId(), e.getAmount(), e.getDescription(), e.getCategory(), e.getNote(), e.getDate());
    }
}
