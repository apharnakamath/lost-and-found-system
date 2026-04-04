package com.lostandfound.repository;

import com.lostandfound.model.Notification;
import com.lostandfound.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * NotificationRepository - Data access layer for Notification entity
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find notifications by user
     */
    List<Notification> findByUser(User user);

    /**
     * Find notifications by user ordered by date
     */
    List<Notification> findByUserOrderByDateDesc(User user);

    /**
     * Find unread notifications by user
     */
    List<Notification> findByUserAndIsReadFalseOrderByDateDesc(User user);

    /**
     * Find read notifications by user
     */
    List<Notification> findByUserAndIsReadTrueOrderByDateDesc(User user);

    /**
     * Find notifications by type
     */
    List<Notification> findByType(String type);

    /**
     * Find notifications within date range
     */
    List<Notification> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count unread notifications by user
     */
    long countByUserAndIsReadFalse(User user);

    /**
     * Custom query to find recent notifications for user
     */
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.date >= :date ORDER BY n.date DESC")
    List<Notification> findRecentNotifications(@Param("user") User user, @Param("date") LocalDateTime date);

    /**
     * Delete old read notifications
     */
    @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.date < :date")
    void deleteOldReadNotifications(@Param("date") LocalDateTime date);
}
