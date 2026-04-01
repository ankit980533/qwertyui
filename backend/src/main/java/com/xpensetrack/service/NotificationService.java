package com.xpensetrack.service;

import com.xpensetrack.model.Notification;
import com.xpensetrack.model.NotificationType;
import com.xpensetrack.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepo;

    public List<Notification> getAll(String userId) {
        return notificationRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnread(String userId) {
        return notificationRepo.findByUserIdAndReadFalse(userId);
    }

    public void markAsRead(String notificationId) {
        var n = notificationRepo.findById(notificationId).orElseThrow();
        n.setRead(true);
        notificationRepo.save(n);
    }

    public void send(String userId, NotificationType type, String title, String message) {
        var n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        notificationRepo.save(n);
    }
}
