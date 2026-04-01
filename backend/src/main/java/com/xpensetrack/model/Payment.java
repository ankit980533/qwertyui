package com.xpensetrack.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Data
@Document(collection = "payments")
public class Payment {
    @Id
    private String id;
    private String fromUserId;
    private String toUserId;
    private double amount;
    private PaymentMethod method;
    private PaymentStatus status = PaymentStatus.PENDING;
    private double cashbackPercent = 0.0;
    private double cashbackAmount = 0.0;
    private Instant createdAt = Instant.now();
}
