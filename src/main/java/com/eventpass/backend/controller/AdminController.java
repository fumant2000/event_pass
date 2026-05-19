package com.eventpass.backend.controller;

import com.eventpass.backend.dto.response.*;
import com.eventpass.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // ← tout le controller est ADMIN uniquement
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ── Demandes organisateur ────────────────────────────────

    /** Liste toutes les demandes en attente */
    @GetMapping("/organizer-requests")
    public ResponseEntity<List<OrganizerRequestResponse>> getPendingRequests() {
        return ResponseEntity.ok(adminService.getPendingOrganizerRequests());
    }

    /** Approuver une demande */
    @PostMapping("/organizer-requests/{userId}/approve")
    public ResponseEntity<MessageResponse> approveOrganizer(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.approveOrganizer(userId));
    }

    /** Rejeter une demande */
    @PostMapping("/organizer-requests/{userId}/reject")
    public ResponseEntity<MessageResponse> rejectOrganizer(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.rejectOrganizer(userId));
    }

    // ── Gestion des utilisateurs ─────────────────────────────

    /** Liste tous les utilisateurs */
    @GetMapping("/users")
    public ResponseEntity<List<UserAdminResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    /** Suspendre un compte */
    @PutMapping("/users/{userId}/suspend")
    public ResponseEntity<MessageResponse> suspendUser(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.suspendUser(userId));
    }

    /** Réactiver un compte */
    @PutMapping("/users/{userId}/activate")
    public ResponseEntity<MessageResponse> activateUser(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.activateUser(userId));
    }

    // ── Tableau de bord ──────────────────────────────────────

    /** Stats globales pour le dashboard admin */
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }
}