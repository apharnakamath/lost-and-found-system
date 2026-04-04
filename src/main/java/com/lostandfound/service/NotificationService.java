package com.lostandfound.service;

import com.lostandfound.model.Claim;
import com.lostandfound.model.Item;
import com.lostandfound.model.Notification;
import com.lostandfound.model.User;
import com.lostandfound.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification createNotification(String message, String type, User user, Item item, Claim claim) {
        Notification notification = new Notification(message, type, user);
        notification.setRelatedItem(item);
        notification.setRelatedClaim(claim);
        
        Notification saved = notificationRepository.save(notification);
        saved.sendNotification(); // Log or send actual notification
        
        log.info("Notification created for user: {}", user.getEmail());
        return saved;
    }

    public List<Notification> getNotificationsByUser(User user) {
        return notificationRepository.findByUserOrderByDateDesc(user);
    }

    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByDateDesc(user);
    }

    public long countUnreadNotifications(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.markAsRead();
        notificationRepository.save(notification);
    }

    public void markAllAsRead(User user) {
        List<Notification> unreadNotifications = getUnreadNotifications(user);
        unreadNotifications.forEach(Notification::markAsRead);
        notificationRepository.saveAll(unreadNotifications);
    }

    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}
