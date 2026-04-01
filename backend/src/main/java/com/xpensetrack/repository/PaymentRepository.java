package com.xpensetrack.repository;

import com.xpensetrack.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    List<Payment> findByFromUserIdOrToUserIdOrderByCreatedAtDesc(String fromUserId, String toUserId);
}
