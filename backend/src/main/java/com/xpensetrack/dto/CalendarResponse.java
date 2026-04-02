package com.xpensetrack.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class CalendarResponse {
    private int year;
    private int month;
    private double dailyBudget;
    private double weeklySpendLimit;
    private double todayTotalSpent;
    private boolean budgetOverrun;
    private List<CalendarDayInfo> days;
    private List<UpcomingEventResponse> upcomingEvents;
    private List<ExpenseResponse> todayExpenses;
}
      //      uri: mongodb+srv://shreya_db_user:Th23cocbase%4022876523@cluster0.39j1zfk.mongodb.net/xpensetrack?retryWrites=true&w=majority
