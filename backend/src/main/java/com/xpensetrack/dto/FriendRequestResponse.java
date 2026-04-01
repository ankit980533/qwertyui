package com.xpensetrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FriendRequestResponse {
    private String id;
    private String userId;
    private String fullName;
    private String displayId;
    private String hostel;
    private String avatarUrl;
    private int mutualFriends;
}
