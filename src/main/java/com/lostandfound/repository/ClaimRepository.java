package com.lostandfound.repository;

import com.lostandfound.model.Claim;
import com.lostandfound.model.Item;
import com.lostandfound.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ClaimRepository - Data access layer for Claim entity
 */
@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    /**
     * Find claims by status
     */
    List<Claim> findByStatus(String status);

    /**
     * Find claims by claimant
     */
    List<Claim> findByClaimant(User claimant);

    /**
     * Find claims by item
     */
    List<Claim> findByItem(Item item);

    /**
     * Find pending claims
     */
    List<Claim> findByStatusOrderByClaimDateDesc(String status);

    /**
     * Find claims by claimant and status
     */
    List<Claim> findByClaimantAndStatus(User claimant, String status);

    /**
     * Find claims within date range
     */
    List<Claim> findByClaimDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Custom query to find all pending claims with item details
     */
    @Query("SELECT c FROM Claim c JOIN FETCH c.item WHERE c.status = 'PENDING' ORDER BY c.claimDate DESC")
    List<Claim> findAllPendingClaimsWithItems();

    /**
     * Find claims by item and status
     */
    @Query("SELECT c FROM Claim c WHERE c.item = :item AND c.status = :status")
    List<Claim> findByItemAndStatus(@Param("item") Item item, @Param("status") String status);

    /**
     * Count claims by status
     */
    long countByStatus(String status);

    /**
     * Count claims by claimant
     */
    long countByClaimant(User claimant);

    /**
     * Find recent claims (last N days)
     */
    @Query("SELECT c FROM Claim c WHERE c.claimDate >= :date ORDER BY c.claimDate DESC")
    List<Claim> findRecentClaims(@Param("date") LocalDateTime date);
}
