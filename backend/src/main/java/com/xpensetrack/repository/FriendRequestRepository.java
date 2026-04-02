package com.xpensetrack.repository;

import com.xpensetrack.model.FriendRequest;
import com.xpensetrack.model.FriendRequestStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends MongoRepository<FriendRequest, String> {
    List<FriendRequest> findByToUserIdAndStatus(String toUserId, FriendRequestStatus status);
    List<FriendRequest> findByFromUserIdAndStatus(String fromUserId, FriendRequestStatus status);
    Optional<FriendRequest> findByFromUserIdAndToUserId(String fromUserId, String toUserId);
}
