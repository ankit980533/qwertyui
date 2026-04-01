package com.xpensetrack.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Data
@Document(collection = "friend_requests")
public class FriendRequest {
    @Id
    private String id;
    private String fromUserId;
    private String toUserId;
    private FriendRequestStatus status = FriendRequestStatus.PENDING;
    private Instant createdAt = Instant.now();
}
