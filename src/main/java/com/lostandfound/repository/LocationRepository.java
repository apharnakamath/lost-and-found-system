package com.lostandfound.repository;

import com.lostandfound.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * LocationRepository - Data access layer for Location entity
 */
@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    /**
     * Find location by place name
     */
    Optional<Location> findByPlaceName(String placeName);

    /**
     * Find location by place name (case insensitive)
     */
    Optional<Location> findByPlaceNameIgnoreCase(String placeName);

    /**
     * Check if location exists by place name
     */
    boolean existsByPlaceName(String placeName);

    /**
     * Find locations by city
     */
    List<Location> findByCity(String city);

    /**
     * Find locations by state
     */
    List<Location> findByState(String state);

    /**
     * Find locations by place name containing (case insensitive)
     */
    List<Location> findByPlaceNameContainingIgnoreCase(String keyword);

    /**
     * Find all locations ordered by place name
     */
    List<Location> findAllByOrderByPlaceNameAsc();

    /**
     * Custom query to count items per location
     */
    @Query("SELECT l.placeName, COUNT(i) FROM Location l LEFT JOIN l.items i GROUP BY l.placeName")
    List<Object[]> countItemsPerLocation();
}
