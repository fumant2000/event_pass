package com.eventpass.backend.controller;

import com.eventpass.backend.dto.request.LoginRequest;
import com.eventpass.backend.dto.request.RegisterRequest;
import com.eventpass.backend.dto.response.AuthResponse;
import com.eventpass.backend.dto.response.MessageResponse;
import com.eventpass.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/request-organizer")
    public ResponseEntity<MessageResponse> requestOrganizer(Principal principal) {
        return ResponseEntity.ok(authService.requestOrganizerStatus(principal.getName()));
    }

    @PostMapping("/approve-organizer/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> approveOrganizer(@PathVariable Long userId) {
        return ResponseEntity.ok(authService.approveOrganizer(userId));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@RequestParam String email) {
        return ResponseEntity.ok(authService.forgotPassword(email));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @RequestParam String token, @RequestParam String newPassword) {
        return ResponseEntity.ok(authService.resetPassword(token, newPassword));
    }
}