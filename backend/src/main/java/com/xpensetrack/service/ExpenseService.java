package com.xpensetrack.service;

import com.xpensetrack.dto.*;
import com.xpensetrack.model.Expense;
import com.xpensetrack.model.ExpenseCategory;
import com.xpensetrack.model.NotificationType;
import com.xpensetrack.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.*;
import java.time.format.DateTimeFormatter;
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

    // All date ranges use UTC day boundaries
    private Instant startOfDayUtc(LocalDate date) {
        return date.atStartOfDay(ZoneOffset.UTC).toInstant();
    }
    private Instant endOfDayUtc(LocalDate date) {
        return date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    }
    private LocalDate toLocalDateUtc(Instant instant) {
        return instant.atZone(ZoneOffset.UTC).toLocalDate();
    }

    public ExpenseResponse addExpense(String userId, AddExpenseRequest req) {
        var expense = new Expense();
        expense.setUserId(userId);
        expense.setAmount(req.getAmount());
        expense.setDescription(req.getDescription());
        expense.setCategory(req.getCategory());
        expense.setNote(req.getNote());

        // Parse date from client (UTC ISO string) or use now
        if (req.getDate() != null && !req.getDate().isEmpty()) {
            expense.setDate(Instant.parse(req.getDate()));
        } else {
            expense.setDate(Instant.now());
        }

        expense.setSplitWithFriendIds(req.getSplitWithFriendIds());
        expense = expenseRepo.save(expense);

        var user = userRepo.findById(userId).orElseThrow();
        user.setCoins(user.getCoins() + 1);
        userRepo.save(user);

        // Check if daily budget exceeded → send overspending alert
        var now = LocalDate.now(ZoneOffset.UTC);
        double dailyBudget = user.getMonthlyBudget() / now.lengthOfMonth();
        var todayStart = startOfDayUtc(now);
        var todayEnd = endOfDayUtc(now);
        double todaySpent = expenseRepo.findByUserIdAndDateRange(userId, todayStart, todayEnd)
                .stream().mapToDouble(Expense::getAmount).sum();
        if (todaySpent > dailyBudget) {
            var notifService = new NotificationService(notificationRepo);
            notifService.send(userId, NotificationType.OVERSPENDING, "Overspending Alert",
                    "You've spent ₹" + (int) todaySpent + " today, exceeding your daily budget of ₹" + (int) dailyBudget + ".");
        }

        // Check dragon happiness → send hungry reminder
        var dragon = dragonRepo.findByUserId(userId).orElse(null);
        if (dragon != null && dragon.getHappiness() < 30) {
            var notifService = new NotificationService(notificationRepo);
            notifService.send(userId, NotificationType.DRAGON_UPDATE, "Reminder",
                    "Hi " + user.getFullName() + "..feed your pet dragon, he's hungry!!");
        }

        return toResponse(expense);
    }

    public List<ExpenseResponse> getExpenses(String userId) {
        return expenseRepo.findByUserIdOrderByDateDesc(userId).stream().map(this::toResponse).toList();
    }

    public DashboardResponse getDashboard(String userId) {
        var user = userRepo.findById(userId).orElseThrow();
        var now = LocalDate.now(ZoneOffset.UTC);
        var monthStart = startOfDayUtc(now.withDayOfMonth(1));
        var monthEnd = endOfDayUtc(now);

        var expenses = expenseRepo.findByUserIdAndDateRange(userId, monthStart, monthEnd);
        var dragon = dragonRepo.findByUserId(userId).orElse(null);
        long unread = notificationRepo.countByUserIdAndReadFalse(userId);

        double monthlySpent = expenses.stream().mapToDouble(Expense::getAmount).sum();
        double remaining = user.getMonthlyBudget() - monthlySpent;
        double usedPct = user.getMonthlyBudget() > 0 ? (monthlySpent / user.getMonthlyBudget()) * 100 : 0;

        Map<ExpenseCategory, Double> breakdown = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategory, Collectors.summingDouble(Expense::getAmount)));

        return DashboardResponse.builder()
                .fullName(user.getFullName()).currentBalance(user.getMonthlyBudget() - monthlySpent)
                .monthlyBudget(user.getMonthlyBudget()).monthlySpent(monthlySpent)
                .remaining(Math.max(remaining, 0)).budgetUsedPercent(Math.min(usedPct, 100))
                .budgetLeftPercent(Math.max(100 - usedPct, 0)).coins(user.getCoins())
                .recentExpenses(expenses.stream()
                        .sorted(Comparator.comparing(Expense::getDate).reversed())
                        .limit(5)
                        .map(this::toResponse)
                        .toList())
                .dragonLevel(dragon != null ? dragon.getLevel() : 1)
                .dragonHappiness(dragon != null ? dragon.getHappiness() : 50)
                .dragonHungry(dragon != null && dragon.getHappiness() < 30)
                .monthlyBreakdown(breakdown).unreadNotificationCount((int) unread)
                .build();
    }

    public ReportResponse getReport(String userId, String period, int year, int month) {
        var user = userRepo.findById(userId).orElseThrow();
        var ym = YearMonth.of(year, month);
        double budget = user.getMonthlyBudget();
        int daysInMonth = ym.lengthOfMonth();
        double dailyBudget = budget / daysInMonth;
        double weeklyBudget = budget / 4.0; // approx 4 weeks

        // Current period expenses
        var start = startOfDayUtc(ym.atDay(1));
        var end = endOfDayUtc(ym.atEndOfMonth());
        var expenses = expenseRepo.findByUserIdAndDateRange(userId, start, end);
        double totalSpent = expenses.stream().mapToDouble(Expense::getAmount).sum();

        // Savings = budget - spent (for the period)
        double totalSaved;
        double prevSaved;
        String periodLabel;

        if ("Weekly".equalsIgnoreCase(period)) {
            // Current week: find which week we're in
            var now = LocalDate.now(ZoneOffset.UTC);
            int dayOfMonth = Math.min(now.getDayOfMonth(), daysInMonth);
            int currentWeek = (dayOfMonth - 1) / 7; // 0-based week index
            var weekStart = startOfDayUtc(ym.atDay(currentWeek * 7 + 1));
            var weekEnd = endOfDayUtc(ym.atDay(Math.min((currentWeek + 1) * 7, daysInMonth)));
            var weekExpenses = expenseRepo.findByUserIdAndDateRange(userId, weekStart, weekEnd);
            double weekSpent = weekExpenses.stream().mapToDouble(Expense::getAmount).sum();
            totalSpent = weekSpent;
            totalSaved = Math.max(weeklyBudget - weekSpent, 0);

            // Previous week
            if (currentWeek > 0) {
                var prevWeekStart = startOfDayUtc(ym.atDay((currentWeek - 1) * 7 + 1));
                var prevWeekEnd = endOfDayUtc(ym.atDay(currentWeek * 7));
                var prevWeekExp = expenseRepo.findByUserIdAndDateRange(userId, prevWeekStart, prevWeekEnd);
                double prevWeekSpent = prevWeekExp.stream().mapToDouble(Expense::getAmount).sum();
                prevSaved = Math.max(weeklyBudget - prevWeekSpent, 0);
            } else {
                // Use last week of previous month
                var prevYm = ym.minusMonths(1);
                int prevDays = prevYm.lengthOfMonth();
                var prevWeekStart = startOfDayUtc(prevYm.atDay(Math.max(prevDays - 6, 1)));
                var prevWeekEnd = endOfDayUtc(prevYm.atEndOfMonth());
                var prevWeekExp = expenseRepo.findByUserIdAndDateRange(userId, prevWeekStart, prevWeekEnd);
                double prevWeekSpent = prevWeekExp.stream().mapToDouble(Expense::getAmount).sum();
                prevSaved = Math.max(weeklyBudget - prevWeekSpent, 0);
            }
            periodLabel = "This Week";
        } else {
            // Monthly: savings = budget - spent
            totalSaved = Math.max(budget - totalSpent, 0);

            // Previous month savings
            var prevYm = ym.minusMonths(1);
            var prevExpenses = expenseRepo.findByUserIdAndDateRange(userId,
                    startOfDayUtc(prevYm.atDay(1)), endOfDayUtc(prevYm.atEndOfMonth()));
            double prevSpent = prevExpenses.stream().mapToDouble(Expense::getAmount).sum();
            prevSaved = Math.max(budget - prevSpent, 0);
            periodLabel = "This Month";
        }

        // Savings change percentage
        double savedChange = prevSaved > 0 ? ((totalSaved - prevSaved) / prevSaved) * 100 : (totalSaved > 0 ? 100 : 0);

        // Monthly spending trend (last 7 months) - budget vs spent
        List<TrendPoint> monthlyTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            var m = ym.minusMonths(i);
            var mExp = expenseRepo.findByUserIdAndDateRange(userId,
                    startOfDayUtc(m.atDay(1)), endOfDayUtc(m.atEndOfMonth()));
            double s = mExp.stream().mapToDouble(Expense::getAmount).sum();
            String label = m.getMonth().name().substring(0, 3);
            label = label.charAt(0) + label.substring(1).toLowerCase();
            monthlyTrend.add(new TrendPoint(label, s, budget, null));
        }

        // Weekly spending (4 weeks of current month)
        List<TrendPoint> weekly = new ArrayList<>();
        for (int w = 0; w < 4; w++) {
            var wStart = startOfDayUtc(ym.atDay(w * 7 + 1));
            var wEnd = endOfDayUtc(ym.atDay(Math.min((w + 1) * 7, daysInMonth)));
            var wExp = expenseRepo.findByUserIdAndDateRange(userId, wStart, wEnd);
            double ws = wExp.stream().mapToDouble(Expense::getAmount).sum();
            weekly.add(new TrendPoint("Week " + (w + 1), ws, null, null));
        }

        // Savings trend (last 6 months) - savings = budget - spent
        List<TrendPoint> savingsTrend = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            var m = ym.minusMonths(i);
            var mExp = expenseRepo.findByUserIdAndDateRange(userId,
                    startOfDayUtc(m.atDay(1)), endOfDayUtc(m.atEndOfMonth()));
            double s = mExp.stream().mapToDouble(Expense::getAmount).sum();
            double saved = Math.max(budget - s, 0);
            String label = m.getMonth().name().substring(0, 3);
            label = label.charAt(0) + label.substring(1).toLowerCase();
            savingsTrend.add(new TrendPoint(label, s, null, saved));
        }

        return ReportResponse.builder()
                .period(period)
                .totalSpent(totalSpent)
                .totalSaved(totalSaved)
                .savedChangePercent(savedChange)
                .monthlySpendingTrend(monthlyTrend)
                .weeklySpending(weekly)
                .savingsTrend(savingsTrend)
                .build();
    }

    public CalendarResponse getCalendar(String userId, int year, int month) {
        var user = userRepo.findById(userId).orElseThrow();
        var ym = YearMonth.of(year, month);
        var start = startOfDayUtc(ym.atDay(1));
        var end = endOfDayUtc(ym.atEndOfMonth());
        var expenses = expenseRepo.findByUserIdAndDateRange(userId, start, end);

        // Daily budget = monthly budget / days in month
        double dailyBudget = user.getMonthlyBudget() / ym.lengthOfMonth();

        Map<LocalDate, Double> dailyTotals = expenses.stream()
                .collect(Collectors.groupingBy(e -> toLocalDateUtc(e.getDate()), Collectors.summingDouble(Expense::getAmount)));

        // Status logic:
        // OVERSPENT (red) = spent > dailyBudget
        // UNDER_BUDGET (green) = spent > 0 but < dailyBudget
        // ON_BUDGET (no color) = spent == dailyBudget (approx)
        // NO_EXPENSE (no color) = no spending that day
        List<CalendarDayInfo> days = new ArrayList<>();
        for (int d = 1; d <= ym.lengthOfMonth(); d++) {
            var date = ym.atDay(d);
            double spent = dailyTotals.getOrDefault(date, 0.0);
            String status;
            if (spent > dailyBudget) {
                status = "SPENT_MOST";      // Red - overspent
            } else if (spent > 0 && spent < dailyBudget) {
                status = "SPENT_LEAST";     // Green - under budget
            } else {
                status = "NORMAL";          // No color - on budget or no expense
            }
            days.add(new CalendarDayInfo(date.toString(), spent, status));
        }

        var upcoming = upcomingEventRepo.findByUserIdAndDueDateGreaterThanEqualOrderByDueDateAsc(userId, LocalDate.now())
                .stream().limit(5).map(e -> new UpcomingEventResponse(e.getId(), e.getTitle(), e.getAmount(), e.getDueDate(), e.isPaid())).toList();

        var todayStart = startOfDayUtc(LocalDate.now(ZoneOffset.UTC));
        var todayEnd = endOfDayUtc(LocalDate.now(ZoneOffset.UTC));
        var today = expenseRepo.findByUserIdAndDateRange(userId, todayStart, todayEnd)
                .stream().map(this::toResponse).toList();

        double todaySpent = today.stream().mapToDouble(ExpenseResponse::getAmount).sum();
        double weeklyLimit = dailyBudget * 7;
        boolean overrun = todaySpent > dailyBudget;

        return CalendarResponse.builder()
                .year(year).month(month)
                .dailyBudget(dailyBudget)
                .weeklySpendLimit(weeklyLimit)
                .todayTotalSpent(todaySpent)
                .budgetOverrun(overrun)
                .days(days)
                .upcomingEvents(upcoming)
                .todayExpenses(today)
                .build();
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(e.getId(), e.getAmount(), e.getDescription(), e.getCategory(), e.getNote(),
                e.getDate() != null ? e.getDate().toString() : "");
    }
}
