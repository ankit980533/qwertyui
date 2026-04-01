package com.xpensetrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FriendSummary {
    private String id;
    private String fullName;
    private String displayId;
    private String email;
    private String hostel;
    private String avatarUrl;
}
