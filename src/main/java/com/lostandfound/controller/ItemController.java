package com.lostandfound.controller;

import com.lostandfound.model.*;
import com.lostandfound.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ItemController {
    private final ItemService itemService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final LocationService locationService;

    @PostMapping("/lost")
    public ResponseEntity<?> reportLostItem(@Valid @RequestBody LostItem lostItem, 
                                            @RequestParam Long reporterId) {
        try {
            User reporter = userService.getUserById(reporterId);
            LostItem saved = itemService.reportLostItem(lostItem, reporter);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/found")
    public ResponseEntity<?> reportFoundItem(@Valid @RequestBody FoundItem foundItem, 
                                             @RequestParam Long reporterId) {
        try {
            User reporter = userService.getUserById(reporterId);
            FoundItem saved = itemService.reportFoundItem(foundItem, reporter);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getItemById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(itemService.getItemById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }

    @GetMapping("/lost")
    public ResponseEntity<List<Item>> getLostItems() {
        return ResponseEntity.ok(itemService.getLostItems());
    }

    @GetMapping("/found")
    public ResponseEntity<List<Item>> getFoundItems() {
        return ResponseEntity.ok(itemService.getFoundItems());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Item>> getItemsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(itemService.getItemsByStatus(status));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Item>> searchItems(@RequestParam String keyword) {
        return ResponseEntity.ok(itemService.searchItems(keyword));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id, @Valid @RequestBody Item item) {
        try {
            return ResponseEntity.ok(itemService.updateItem(id, item));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateItemStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            itemService.updateItemStatus(id, status);
            return ResponseEntity.ok(Map.of("message", "Status updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id) {
        try {
            itemService.deleteItem(id);
            return ResponseEntity.ok(Map.of("message", "Item deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
