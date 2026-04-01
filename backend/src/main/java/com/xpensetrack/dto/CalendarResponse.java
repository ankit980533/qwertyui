package com.xpensetrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class CalendarResponse {
    private int year;
    private int month;
    private List<CalendarDayInfo> days;
    private List<UpcomingEventResponse> upcomingEvents;
    private List<ExpenseResponse> todayExpenses;
}

