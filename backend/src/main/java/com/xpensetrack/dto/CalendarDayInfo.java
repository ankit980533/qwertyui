package com.xpensetrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CalendarDayInfo {
    private String date;
    private double totalSpent;
    private String status;
}
