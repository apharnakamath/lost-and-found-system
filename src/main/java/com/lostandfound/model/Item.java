package com.lostandfound.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Item Entity - Base class for LostItem and FoundItem
 * Contains common attributes and methods for all items
 */
@Entity
@Table(name = "items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "item_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long itemId;

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200)
    @Column(nullable = false)
    protected String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 1000)
    @Column(nullable = false, length = 1000)
    protected String description;

    @Column(nullable = false)
    protected LocalDateTime dateReported;

    @Column(nullable = false)
    protected String status = "PENDING"; // PENDING, VERIFIED, MATCHED, CLAIMED, CLOSED

    @Column(length = 500)
    protected String image; // Path to image file

    @CreatedDate
    @Column(nullable = false, updatable = false)
    protected LocalDateTime createdAt;

    @LastModifiedDate
    protected LocalDateTime updatedAt;

    // Relationships
   @JsonIgnoreProperties({"reportedItems", "claims", "notifications", "hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    protected User reporter;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    protected Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id", nullable = false)
    protected Location location;

    @JsonIgnoreProperties({"item", "hibernateLazyInitializer", "handler"})
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    protected List<Claim> claims = new ArrayList<>();

    // Business Methods
    
    /**
     * Update the status of the item
     */
    public void updateStatus(String newStatus) {
        this.status = newStatus;
    }

    /**
     * Get detailed information about the item
     */
    public String getDetails() {
        return String.format("Item[id=%d, title='%s', status='%s', category='%s', location='%s']",
                itemId, title, status,
                category != null ? category.getCategoryName() : "N/A",
                location != null ? location.getPlaceName() : "N/A");
    }

    /**
     * Abstract method to get item type (LOST or FOUND)
     * Must be implemented by subclasses
     */
    public abstract String getItemType();
}
