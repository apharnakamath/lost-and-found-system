package com.lostandfound.service;

import com.lostandfound.model.Admin;
import com.lostandfound.model.Claim;
import com.lostandfound.model.Item;
import com.lostandfound.model.User;
import com.lostandfound.repository.ClaimRepository;
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
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ItemService itemService;
    private final NotificationService notificationService;

    public Claim submitClaim(Claim claim, User claimant, Long itemId) {
        log.info("Submitting claim for item: {} by user: {}", itemId, claimant.getEmail());
        
        Item item = itemService.getItemById(itemId);
        
        claim.setClaimant(claimant);
        claim.setItem(item);
        claim.setClaimDate(LocalDateTime.now());
        claim.setStatus("PENDING");
        
        Claim savedClaim = claimRepository.save(claim);
        
        // Notify the claimant
        notificationService.createNotification(
            "Your claim for item '" + item.getTitle() + "' has been submitted and is pending approval.",
            "INFO",
            claimant,
            item,
            savedClaim
        );
        
        // Notify the item reporter
        notificationService.createNotification(
            "A claim has been made for your item '" + item.getTitle() + "'.",
            "INFO",
            item.getReporter(),
            item,
            savedClaim
        );
        
        return savedClaim;
    }

    public Claim approveClaim(Long claimId, Admin admin) {
        log.info("Approving claim: {} by admin: {}", claimId, admin.getEmail());
        
        Claim claim = getClaimById(claimId);
        claim.approve(admin);
        
        Claim approved = claimRepository.save(claim);
        
        // Notify the claimant
        notificationService.createNotification(
            "Congratulations! Your claim for item '" + claim.getItem().getTitle() + "' has been approved.",
            "SUCCESS",
            claim.getClaimant(),
            claim.getItem(),
            approved
        );
        
        // Notify the item reporter
        notificationService.createNotification(
            "A claim for your item '" + claim.getItem().getTitle() + "' has been approved.",
            "INFO",
            claim.getItem().getReporter(),
            claim.getItem(),
            approved
        );
        
        return approved;
    }

    public Claim rejectClaim(Long claimId, Admin admin, String reason) {
        log.info("Rejecting claim: {} by admin: {}", claimId, admin.getEmail());
        
        Claim claim = getClaimById(claimId);
        claim.reject(admin, reason);
        
        Claim rejected = claimRepository.save(claim);
        
        // Notify the claimant
        notificationService.createNotification(
            "Your claim for item '" + claim.getItem().getTitle() + "' has been rejected. Reason: " + reason,
            "WARNING",
            claim.getClaimant(),
            claim.getItem(),
            rejected
        );
        
        return rejected;
    }

    public Claim getClaimById(Long claimId) {
        return claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found with id: " + claimId));
    }

    public List<Claim> getAllClaims() {
        return claimRepository.findAll();
    }

    public List<Claim> getClaimsByStatus(String status) {
        return claimRepository.findByStatus(status);
    }

    public List<Claim> getPendingClaims() {
        return claimRepository.findAllPendingClaimsWithItems();
    }

    public List<Claim> getClaimsByClaimant(User claimant) {
        return claimRepository.findByClaimant(claimant);
    }

    public List<Claim> getClaimsByItem(Item item) {
        return claimRepository.findByItem(item);
    }

    public List<Claim> getRecentClaims(int days) {
        LocalDateTime date = LocalDateTime.now().minusDays(days);
        return claimRepository.findRecentClaims(date);
    }

    public long countClaimsByStatus(String status) {
        return claimRepository.countByStatus(status);
    }

    public void deleteClaim(Long claimId) {
        Claim claim = getClaimById(claimId);
        claimRepository.delete(claim);
        log.info("Claim deleted: {}", claimId);
    }
}
