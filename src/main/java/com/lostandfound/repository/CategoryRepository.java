package com.lostandfound.repository;

import com.lostandfound.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CategoryRepository - Data access layer for Category entity
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find category by name
     */
    Optional<Category> findByCategoryName(String categoryName);

    /**
     * Find category by name (case insensitive)
     */
    Optional<Category> findByCategoryNameIgnoreCase(String categoryName);

    /**
     * Check if category exists by name
     */
    boolean existsByCategoryName(String categoryName);

    /**
     * Find categories by name containing (case insensitive)
     */
    List<Category> findByCategoryNameContainingIgnoreCase(String keyword);

    /**
     * Find all categories ordered by name
     */
    List<Category> findAllByOrderByCategoryNameAsc();

    /**
     * Custom query to count items per category
     */
    @Query("SELECT c.categoryName, COUNT(i) FROM Category c LEFT JOIN c.items i GROUP BY c.categoryName")
    List<Object[]> countItemsPerCategory();
}
