package com.lostandfound.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

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

    @JsonIgnoreProperties({"claims", "reporter", "hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @JsonIgnoreProperties({"claims", "reportedItems", "notifications", "password", "hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "claimant_id", nullable = false)
    private User claimant;

    @Column(length = 1000)
    private String proofDescription; 

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, DECLINED

    // THE FIX: Renamed back to claimDate and mapped specifically to your claim_date database column
    @CreatedDate
    @Column(name = "claim_date", nullable = false, updatable = false)
    private LocalDateTime claimDate;

    @PrePersist
    protected void onCreate() {
        if (claimDate == null) {
            claimDate = LocalDateTime.now();
        }
    }
}
