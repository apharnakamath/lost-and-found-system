package com.lostandfound.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Admin Entity - Extends User with administrative privileges
 * Inherits all User attributes and methods
 */
@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("ADMIN")
public class Admin extends User {

    public Admin(String name, String email, String phone, String password) {
        super();
        setName(name);
        setEmail(email);
        setPhone(phone);
        setPassword(password);
        setRole("ADMIN");
    }

    // Admin-specific business methods
    
    /**
     * Verify an item reported by a user
     */
    public void verifyItem(Item item) {
        item.setStatus("VERIFIED");
    }

    /**
     * Approve a claim
     */
    public void approveClaim(Claim claim) {
        claim.setStatus("APPROVED");
    }

    /**
     * Reject a claim
     */
    public void rejectClaim(Claim claim) {
        claim.setStatus("REJECTED");
    }

    /**
     * Match lost and found items
     * This is a placeholder - actual implementation would use 
     * matching algorithm based on description, category, location, date
     */
    public void matchItems(Item lostItem, Item foundItem) {
        // Matching logic would be implemented in service layer
        System.out.println("Matching items: " + lostItem.getTitle() + " with " + foundItem.getTitle());
    }
}
