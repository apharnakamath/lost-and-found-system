package com.lostandfound.service;

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

    public Claim approveClaim(Long claimId, Long reporterId) {
        log.info("Approving claim: {} by reporter ID: {}", claimId, reporterId);
        
        Claim claim = getClaimById(claimId);
        Item item = claim.getItem();

        // SECURITY: Ensure the person approving is the original reporter
        if (!item.getReporter().getUserId().equals(reporterId)) {
            throw new RuntimeException("Unauthorized: Only the user who reported this item can approve the claim.");
        }
        
        claim.setStatus("APPROVED");
        
        // Automatically close the item since it has been successfully claimed
        item.setStatus("CLAIMED");
        
        Claim approved = claimRepository.save(claim);
        
        // Notify the claimant
        notificationService.createNotification(
            "Congratulations! Your claim for item '" + item.getTitle() + "' has been approved.",
            "SUCCESS",
            claim.getClaimant(),
            item,
            approved
        );
        
        // Notify the item reporter
        notificationService.createNotification(
            "You have approved the claim for your item '" + item.getTitle() + "'.",
            "INFO",
            item.getReporter(),
            item,
            approved
        );
        
        return approved;
    }

    public Claim rejectClaim(Long claimId, Long reporterId, String reason) {
        log.info("Rejecting claim: {} by reporter ID: {}", claimId, reporterId);
        
        Claim claim = getClaimById(claimId);
        Item item = claim.getItem();

        // SECURITY: Ensure the person rejecting is the original reporter
        if (!item.getReporter().getUserId().equals(reporterId)) {
            throw new RuntimeException("Unauthorized: Only the user who reported this item can decline the claim.");
        }
        
        claim.setStatus("DECLINED");
        
        Claim rejected = claimRepository.save(claim);
        
        // Notify the claimant
        notificationService.createNotification(
            "Your claim for item '" + item.getTitle() + "' has been declined. Reason: " + reason,
            "WARNING",
            claim.getClaimant(),
            item,
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
        return claimRepository.findAllPendingClaimsWithItems(); // Keep if you have this custom query, otherwise change to findByStatus("PENDING")
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
