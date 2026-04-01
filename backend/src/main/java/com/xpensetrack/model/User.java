package com.xpensetrack.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String fullName;
    private String phoneNumber;
    @Indexed(unique = true)
    private String email;
    private String password;
    private String avatarUrl;
    private String address;
    private String hostel;
    private int coins = 0;
    private double currentBalance = 0.0;
    private double monthlyBudget = 5000.0;
    private List<String> friendIds = new ArrayList<>();
    private boolean termsAccepted = false;
    private String googleId;
    private Instant createdAt = Instant.now();

    public String getDisplayId() {
        if (id == null) return "HL000000";
        String suffix = id.length() > 6 ? id.substring(id.length() - 6).toUpperCase() : id.toUpperCase();
        return "HL" + suffix;
    }

    public String getJoinedMonth() {
        YearMonth ym = YearMonth.from(createdAt.atZone(ZoneId.systemDefault()));
        String month = ym.getMonth().name();
        return month.charAt(0) + month.substring(1).toLowerCase() + " " + ym.getYear();
    }
}
