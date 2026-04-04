package com.lostandfound.service;

import com.lostandfound.model.*;
import com.lostandfound.repository.ItemRepository;
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
public class ItemService {

    private final ItemRepository itemRepository;
    private final NotificationService notificationService;

    public LostItem reportLostItem(LostItem lostItem, User reporter) {
        log.info("Reporting lost item: {} by user: {}", lostItem.getTitle(), reporter.getEmail());
        lostItem.setReporter(reporter);
        lostItem.setDateReported(LocalDateTime.now());
        lostItem.setStatus("PENDING");
        
        LostItem saved = (LostItem) itemRepository.save(lostItem);
        
        // Send notification
        notificationService.createNotification(
            "Your lost item '" + lostItem.getTitle() + "' has been reported successfully.",
            "SUCCESS",
            reporter,
            saved,
            null
        );
        
        return saved;
    }

    public FoundItem reportFoundItem(FoundItem foundItem, User reporter) {
        log.info("Reporting found item: {} by user: {}", foundItem.getTitle(), reporter.getEmail());
        foundItem.setReporter(reporter);
        foundItem.setDateReported(LocalDateTime.now());
        foundItem.setStatus("PENDING");
        
        FoundItem saved = (FoundItem) itemRepository.save(foundItem);
        
        // Send notification
        notificationService.createNotification(
            "Your found item '" + foundItem.getTitle() + "' has been reported successfully.",
            "SUCCESS",
            reporter,
            saved,
            null
        );
        
        return saved;
    }

    public Item getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + itemId));
    }

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public List<Item> getLostItems() {
        return itemRepository.findAllLostItems();
    }

    public List<Item> getFoundItems() {
        return itemRepository.findAllFoundItems();
    }

    public List<Item> getItemsByStatus(String status) {
        return itemRepository.findByStatus(status);
    }

    public List<Item> getItemsByCategory(Category category) {
        return itemRepository.findByCategory(category);
    }

    public List<Item> getItemsByLocation(Location location) {
        return itemRepository.findByLocation(location);
    }

    public List<Item> getItemsByReporter(User reporter) {
        return itemRepository.findByReporter(reporter);
    }

    public List<Item> searchItems(String keyword) {
        return itemRepository.searchByKeyword(keyword);
    }

    public List<Item> getRecentItems(int days) {
        LocalDateTime date = LocalDateTime.now().minusDays(days);
        return itemRepository.findRecentItems(date);
    }

    public Item updateItem(Long itemId, Item updatedItem) {
        Item item = getItemById(itemId);
        
        item.setTitle(updatedItem.getTitle());
        item.setDescription(updatedItem.getDescription());
        item.setCategory(updatedItem.getCategory());
        item.setLocation(updatedItem.getLocation());
        
        return itemRepository.save(item);
    }

    public void updateItemStatus(Long itemId, String status) {
        Item item = getItemById(itemId);
        item.updateStatus(status);
        itemRepository.save(item);
        
        // Notify the reporter
        notificationService.createNotification(
            "Status of your item '" + item.getTitle() + "' has been updated to: " + status,
            "INFO",
            item.getReporter(),
            item,
            null
        );
    }

    public void deleteItem(Long itemId) {
        Item item = getItemById(itemId);
        itemRepository.delete(item);
        log.info("Item deleted: {}", itemId);
    }

    public List<Item> matchLostWithFound(LostItem lostItem) {
        log.info("Attempting to match lost item: {}", lostItem.getTitle());
        
        // Get all verified found items in same category
        List<Item> potentialMatches = itemRepository.findVerifiedItemsByCategory(lostItem.getCategory());
        
        // Filter by location and time proximity (simple matching algorithm)
        return potentialMatches.stream()
                .filter(item -> item instanceof FoundItem)
                .filter(item -> item.getLocation().equals(lostItem.getLocation()))
                .filter(item -> {
                    FoundItem foundItem = (FoundItem) item;
                    return foundItem.getDateFound().isAfter(lostItem.getDateLost().minusDays(7)) &&
                           foundItem.getDateFound().isBefore(lostItem.getDateLost().plusDays(7));
                })
                .toList();
    }
}
