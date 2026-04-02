package com.xpensetrack.service;

import com.xpensetrack.dto.PaymentResponse;
import com.xpensetrack.model.*;
import com.xpensetrack.repository.PaymentRepository;
import com.xpensetrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;

    private static final Map<PaymentMethod, Double> CASHBACK_RATES = Map.of(
            PaymentMethod.UPI, 0.30, PaymentMethod.CREDIT_CARD, 0.05,
            PaymentMethod.DEBIT_CARD, 0.05, PaymentMethod.ATM_CARD, 0.0,
            PaymentMethod.CASH, 0.0, PaymentMethod.ONLINE, 0.0);

    public PaymentResponse initiatePayment(String userId, String toUserId, double amount, PaymentMethod method) {
        var toUser = userRepo.findById(toUserId).orElseThrow();
        double rate = CASHBACK_RATES.getOrDefault(method, 0.0);
        var payment = new Payment();
        payment.setFromUserId(userId);
        payment.setToUserId(toUserId);
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setCashbackPercent(rate * 100);
        payment.setCashbackAmount(amount * rate);
        payment = paymentRepo.save(payment);

        // Notify recipient
        var fromUser = userRepo.findById(userId).orElse(null);
        String fromName = fromUser != null ? fromUser.getFullName() : "Someone";
        notificationService.send(toUserId, NotificationType.PAYMENT_SUCCESS, "Payment Successful",
                "Transaction between you and " + fromName + " was successful.");
        notificationService.send(userId, NotificationType.PAYMENT_SUCCESS, "Payment Successful",
                "Transaction between you and " + toUser.getFullName() + " was successful.");

        return PaymentResponse.builder().id(payment.getId()).toUserName(toUser.getFullName())
                .amount(amount).method(method).status("COMPLETED")
                .cashbackPercent(rate * 100).cashbackAmount(amount * rate).secure(true).build();
    }

    public List<PaymentResponse> getHistory(String userId) {
        return paymentRepo.findByFromUserIdOrToUserIdOrderByCreatedAtDesc(userId, userId).stream().map(p -> {
            var toUser = userRepo.findById(p.getToUserId()).orElse(null);
            return PaymentResponse.builder().id(p.getId()).toUserName(toUser != null ? toUser.getFullName() : "Unknown")
                    .amount(p.getAmount()).method(p.getMethod()).status(p.getStatus().name())
                    .cashbackPercent(p.getCashbackPercent()).cashbackAmount(p.getCashbackAmount()).secure(true).build();
        }).toList();
    }
}
