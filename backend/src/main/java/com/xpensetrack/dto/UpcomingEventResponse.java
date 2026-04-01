package com.xpensetrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class UpcomingEventResponse {
    private String id;
    private String title;
    private double amount;
    private LocalDate dueDate;
    private boolean paid;
}
