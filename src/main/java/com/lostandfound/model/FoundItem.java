package com.lostandfound.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * FoundItem Entity - Represents an item that was found
 * Extends Item with additional found-specific attributes
 */
@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("FOUND")
public class FoundItem extends Item {

    private LocalDateTime dateFound;

    @Column(length = 500)
    private String currentLocation; // Where the found item is currently stored

    public FoundItem(String title, String description, LocalDateTime dateFound, 
                     Category category, Location location, User reporter) {
        super();
        this.title = title;
        this.description = description;
        this.dateFound = dateFound;
        this.dateReported = LocalDateTime.now();
        this.category = category;
        this.location = location;
        this.reporter = reporter;
        this.status = "PENDING";
    }

    @Override
    public String getItemType() {
        return "FOUND";
    }

    @Override
    public String getDetails() {
        return String.format("FoundItem[id=%d, title='%s', dateFound=%s, status='%s', category='%s', location='%s']",
                itemId, title, dateFound, status,
                category != null ? category.getCategoryName() : "N/A",
                location != null ? location.getPlaceName() : "N/A");
    }
}
