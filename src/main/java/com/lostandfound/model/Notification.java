package com.lostandfound.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Notification Entity - Represents notifications sent to users
 * Implements Observer Pattern for the notification system
 */
@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @NotBlank(message = "Message is required")
    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private String type; // INFO, SUCCESS, WARNING, ERROR

    @Column(nullable = false)
    private Boolean isRead = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_item_id")
    private Item relatedItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_claim_id")
    private Claim relatedClaim;

    public Notification(String message, String type, User user) {
        this.message = message;
        this.type = type;
        this.user = user;
        this.date = LocalDateTime.now();
        this.isRead = false;
    }

    // Business Methods
    
    /**
     * Send notification to user
     * In real implementation, this could send email, SMS, push notification
     */
    public void sendNotification() {
        // Placeholder for actual notification sending logic
        System.out.println("Sending notification to " + user.getEmail() + ": " + message);
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public void markAsUnread() {
        this.isRead = false;
    }
}
