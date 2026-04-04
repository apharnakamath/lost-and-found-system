package com.lostandfound.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * LostItem Entity - Represents an item that was lost
 * Extends Item with additional lost-specific attributes
 */
@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("LOST")
public class LostItem extends Item {

    private LocalDateTime dateLost;

    @Column(length = 500)
    private String rewardOffered; // Optional reward description

    public LostItem(String title, String description, LocalDateTime dateLost, 
                    Category category, Location location, User reporter) {
        super();
        this.title = title;
        this.description = description;
        this.dateLost = dateLost;
        this.dateReported = LocalDateTime.now();
        this.category = category;
        this.location = location;
        this.reporter = reporter;
        this.status = "PENDING";
    }

    @Override
    public String getItemType() {
        return "LOST";
    }

    @Override
    public String getDetails() {
        return String.format("LostItem[id=%d, title='%s', dateLost=%s, status='%s', category='%s', location='%s']",
                itemId, title, dateLost, status,
                category != null ? category.getCategoryName() : "N/A",
                location != null ? location.getPlaceName() : "N/A");
    }
}
