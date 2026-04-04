package com.lostandfound.repository;

import com.lostandfound.model.Category;
import com.lostandfound.model.Item;
import com.lostandfound.model.Location;
import com.lostandfound.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ItemRepository - Data access layer for Item entity
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * Find items by status
     */
    List<Item> findByStatus(String status);

    /**
     * Find items by category
     */
    List<Item> findByCategory(Category category);

    /**
     * Find items by location
     */
    List<Item> findByLocation(Location location);

    /**
     * Find items by reporter
     */
    List<Item> findByReporter(User reporter);

    /**
     * Find items by title containing (case insensitive)
     */
    List<Item> findByTitleContainingIgnoreCase(String title);

    /**
     * Find items by description containing (case insensitive)
     */
    List<Item> findByDescriptionContainingIgnoreCase(String description);

    /**
     * Find items reported within date range
     */
    List<Item> findByDateReportedBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find items by category and status
     */
    List<Item> findByCategoryAndStatus(Category category, String status);

    /**
     * Find items by location and status
     */
    List<Item> findByLocationAndStatus(Location location, String status);

    /**
     * Custom query to find lost items
     */
    @Query("SELECT i FROM Item i WHERE TYPE(i) = LostItem")
    List<Item> findAllLostItems();

    /**
     * Custom query to find found items
     */
    @Query("SELECT i FROM Item i WHERE TYPE(i) = FoundItem")
    List<Item> findAllFoundItems();

    /**
     * Search items by keyword in title or description
     */
    @Query("SELECT i FROM Item i WHERE LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Item> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Find recent items (last N days)
     */
    @Query("SELECT i FROM Item i WHERE i.dateReported >= :date ORDER BY i.dateReported DESC")
    List<Item> findRecentItems(@Param("date") LocalDateTime date);

    /**
     * Find verified items by category
     */
    @Query("SELECT i FROM Item i WHERE i.category = :category AND i.status = 'VERIFIED'")
    List<Item> findVerifiedItemsByCategory(@Param("category") Category category);
}
