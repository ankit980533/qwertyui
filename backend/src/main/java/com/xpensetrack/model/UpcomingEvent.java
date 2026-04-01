package com.xpensetrack.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

@Data
@Document(collection = "upcoming_events")
public class UpcomingEvent {
    @Id
    private String id;
    private String userId;
    private String title;
    private double amount;
    private LocalDate dueDate;
    private boolean recurring = false;
    private boolean paid = false;
}
