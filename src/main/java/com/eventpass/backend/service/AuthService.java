package com.eventpass.backend.service;

import com.eventpass.backend.config.JwtService;
import com.eventpass.backend.dto.request.LoginRequest;
import com.eventpass.backend.dto.request.RegisterRequest;
import com.eventpass.backend.dto.response.AuthResponse;
import com.eventpass.backend.dto.response.MessageResponse;
import com.eventpass.backend.entity.User;
import com.eventpass.backend.enums.Role;
import com.eventpass.backend.enums.UserStatus;
import com.eventpass.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail()))
            throw new RuntimeException("Email déjà utilisé");

        User user = userRepository.save(User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .build());

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        return buildAuthResponse(user);
    }

    public MessageResponse requestOrganizerStatus(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (user.getRole() == Role.ORGANIZER)
            return MessageResponse.error("Vous êtes déjà organisateur");

        user.setStatus(UserStatus.PENDING_ORGANIZER);
        userRepository.save(user);
        return MessageResponse.ok("Demande de statut organisateur soumise");
    }

    public MessageResponse approveOrganizer(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        user.setRole(Role.ORGANIZER);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        return MessageResponse.ok("Utilisateur promu organisateur");
    }

    private AuthResponse buildAuthResponse(User user) {
        return AuthResponse.builder()
                .token(jwtService.generateToken(user))
                .type("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public MessageResponse forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email introuvable"));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);
        return MessageResponse.ok("Token de reset : " + token);
    }

    public MessageResponse resetPassword(String token, String newPassword) {
        User user = userRepository.findAll().stream()
                .filter(u -> token.equals(u.getResetToken()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Token expiré");

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return MessageResponse.ok("Mot de passe mis à jour");
    }
}
