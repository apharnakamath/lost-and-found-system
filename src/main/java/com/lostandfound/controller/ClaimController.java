package com.lostandfound.controller;

import com.lostandfound.model.*;
import com.lostandfound.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ClaimController {
    private final ClaimService claimService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> submitClaim(@Valid @RequestBody Claim claim, 
                                        @RequestParam Long claimantId, 
                                        @RequestParam Long itemId) {
        try {
            User claimant = userService.getUserById(claimantId);
            Claim saved = claimService.submitClaim(claim, claimant, itemId);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approveClaim(@PathVariable Long id, @RequestParam Long reporterId) {
        try {
            Claim approved = claimService.approveClaim(id, reporterId);
            return ResponseEntity.ok(approved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> rejectClaim(@PathVariable Long id, 
                                        @RequestParam Long reporterId, 
                                        @RequestParam(required = false, defaultValue = "Item belongs to someone else") String reason) {
        try {
            Claim rejected = claimService.rejectClaim(id, reporterId, reason);
            return ResponseEntity.ok(rejected);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getClaimById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(claimService.getClaimById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Claim>> getAllClaims() {
        return ResponseEntity.ok(claimService.getAllClaims());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Claim>> getPendingClaims() {
        return ResponseEntity.ok(claimService.getPendingClaims());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Claim>> getClaimsByUser(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(claimService.getClaimsByClaimant(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClaim(@PathVariable Long id) {
        try {
            claimService.deleteClaim(id);
            return ResponseEntity.ok(Map.of("message", "Claim deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
