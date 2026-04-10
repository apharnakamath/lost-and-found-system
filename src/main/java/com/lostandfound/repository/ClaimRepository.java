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

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    
    List<Claim> findByStatus(String status);
    
    List<Claim> findByClaimant(User claimant);
    
    List<Claim> findByItem(Item item);
    
    long countByStatus(String status);

    List<Claim> findByClaimDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT c FROM Claim c JOIN FETCH c.item WHERE c.status = 'PENDING'")
    List<Claim> findAllPendingClaimsWithItems();

    // THE FINAL FIX: Changed c.createdAt back to c.claimDate in the query!
    @Query("SELECT c FROM Claim c WHERE c.claimDate >= :date")
    List<Claim> findRecentClaims(@Param("date") LocalDateTime date);
}
