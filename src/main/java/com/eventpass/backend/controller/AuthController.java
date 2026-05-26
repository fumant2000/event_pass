package com.eventpass.backend.controller;

import com.eventpass.backend.config.JwtService;
import com.eventpass.backend.dto.request.LoginRequest;
import com.eventpass.backend.dto.request.RegisterRequest;
import com.eventpass.backend.dto.response.AuthResponse;
import com.eventpass.backend.dto.response.MessageResponse;
import com.eventpass.backend.entity.User;
import com.eventpass.backend.enums.UserStatus;
import com.eventpass.backend.repository.UserRepository;
import com.eventpass.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository; 
    private final JwtService jwtService; 

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

    @GetMapping("/pending-organizers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getPendingOrganizers() {
        List<Map<String, Object>> result = userRepository
                .findByStatus(UserStatus.PENDING_ORGANIZER)
                .stream()
                .map(u -> {
                    Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id", u.getId());
                    m.put("name", u.getName());
                    m.put("email", u.getEmail());
                    m.put("phone", u.getPhone() != null ? u.getPhone() : "");
                    m.put("role", u.getRole().name());
                    return m;
                })
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(result);
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

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getMe(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        String freshToken = jwtService.generateToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(freshToken)
                .type("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .build());
    }
}