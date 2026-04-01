package com.xpensetrack.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Data
@Document(collection = "notifications")
public class Notification {
    @Id
    private String id;
    private String userId;
    private NotificationType type;
    private String title;
    private String message;
    private String avatarUrl;
    private boolean read = false;
    private Instant createdAt = Instant.now();
}
