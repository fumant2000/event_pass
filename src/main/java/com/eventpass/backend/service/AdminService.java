package com.eventpass.backend.service;

import com.eventpass.backend.dto.response.*;
import com.eventpass.backend.entity.User;
import com.eventpass.backend.enums.EventStatus;
import com.eventpass.backend.enums.Role;
import com.eventpass.backend.enums.TicketStatus;
import com.eventpass.backend.enums.UserStatus;
import com.eventpass.backend.dto.response.UserAdminResponse;
import com.eventpass.backend.repository.EventRepository;
import com.eventpass.backend.repository.TicketRepository;
import com.eventpass.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;

    // ── Demandes organisateur ────────────────────────────────

    public List<OrganizerRequestResponse> getPendingOrganizerRequests() {
        return userRepository.findByStatus(UserStatus.PENDING_ORGANIZER)
                .stream()
                .map(this::toOrganizerRequest)
                .collect(Collectors.toList());
    }

    public MessageResponse approveOrganizer(Long userId) {
        User user = findUserOrThrow(userId);

        if (user.getStatus() != UserStatus.PENDING_ORGANIZER)
            return MessageResponse.error("Cet utilisateur n'a pas de demande en attente");

        user.setRole(Role.ORGANIZER);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        return MessageResponse.ok("✅ " + user.getName() + " est maintenant organisateur");
    }

    public MessageResponse rejectOrganizer(Long userId) {
        User user = findUserOrThrow(userId);

        if (user.getStatus() != UserStatus.PENDING_ORGANIZER)
            return MessageResponse.error("Cet utilisateur n'a pas de demande en attente");

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        return MessageResponse.ok("Demande de " + user.getName() + " refusée");
    }

    // ── Gestion des utilisateurs ─────────────────────────────

    public List<UserAdminResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toUserAdmin)
                .collect(Collectors.toList());
    }

    public MessageResponse suspendUser(Long userId) {
        User user = findUserOrThrow(userId);

        if (user.getRole() == Role.ADMIN)
            return MessageResponse.error("Impossible de suspendre un administrateur");

        user.setStatus(UserStatus.SUSPENDED);
        userRepository.save(user);
        return MessageResponse.ok("Compte de " + user.getName() + " suspendu");
    }

    public MessageResponse activateUser(Long userId) {
        User user = findUserOrThrow(userId);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        return MessageResponse.ok("Compte de " + user.getName() + " réactivé");
    }

    // ── Tableau de bord ──────────────────────────────────────

    public AdminStatsResponse getStats() {
        return AdminStatsResponse.builder()
                .totalUsers(userRepository.countByRole(Role.USER))
                .totalOrganizers(userRepository.countByRole(Role.ORGANIZER))
                .pendingOrganizerRequests(userRepository.countByStatus(UserStatus.PENDING_ORGANIZER))
                .totalEvents(eventRepository.count())
                .activeEvents(eventRepository.findByStatus(EventStatus.PUBLISHED).size())
                .totalTicketsSold(ticketRepository.countByStatus(TicketStatus.VALID)
                        + ticketRepository.countByStatus(TicketStatus.USED))
                .totalRevenue(ticketRepository.sumRevenue())
                .build();
    }

    // ── Helpers ──────────────────────────────────────────────

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    private OrganizerRequestResponse toOrganizerRequest(User u) {
        return OrganizerRequestResponse.builder()
                .userId(u.getId()).name(u.getName()).email(u.getEmail())
                .phone(u.getPhone()).status(u.getStatus()).createdAt(u.getCreatedAt())
                .build();
    }

    private UserAdminResponse toUserAdmin(User u) {
        return UserAdminResponse.builder()
                .id(u.getId()).name(u.getName()).email(u.getEmail())
                .phone(u.getPhone()).role(u.getRole()).status(u.getStatus())
                .createdAt(u.getCreatedAt())
                .build();
    }
}