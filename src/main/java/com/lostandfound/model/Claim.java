package com.lostandfound.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Claim Entity - Represents a claim made by a user for an item
 */
@Entity
@Table(name = "claims")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long claimId;

    @Column(nullable = false)
    private LocalDateTime claimDate;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @NotBlank(message = "Proof description is required")
    @Column(nullable = false, length = 1000)
    private String proofDescription; // Description of proof of ownership

    @Column(length = 500)
    private String proofDocument; // Path to uploaded proof document

    @Column(length = 1000)
    private String adminNotes; // Notes added by admin during verification

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claimant_id", nullable = false)
    private User claimant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private Admin verifiedBy;

    // Business Methods
    
    /**
     * Submit a new claim
     */
    public void submitClaim() {
        this.claimDate = LocalDateTime.now();
        this.status = "PENDING";
    }

    /**
     * Approve this claim
     */
    public void approve(Admin admin) {
        this.status = "APPROVED";
        this.verifiedBy = admin;
        this.item.updateStatus("CLAIMED");
    }

    /**
     * Reject this claim
     */
    public void reject(Admin admin, String reason) {
        this.status = "REJECTED";
        this.verifiedBy = admin;
        this.adminNotes = reason;
    }

    public boolean isPending() {
        return "PENDING".equals(this.status);
    }

    public boolean isApproved() {
        return "APPROVED".equals(this.status);
    }

    public boolean isRejected() {
        return "REJECTED".equals(this.status);
    }
}
