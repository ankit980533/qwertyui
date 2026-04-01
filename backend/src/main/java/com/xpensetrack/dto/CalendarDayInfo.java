package com.xpensetrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class CalendarDayInfo {
    private LocalDate date;
    private double totalSpent;
    private String status; // SPENT_MOST, SPENT_LEAST, NORMAL
}
