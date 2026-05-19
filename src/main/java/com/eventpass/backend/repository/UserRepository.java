package com.eventpass.backend.repository;

import com.eventpass.backend.entity.User;
import com.eventpass.backend.enums.Role;
import com.eventpass.backend.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // ── Nouveaux ──────────────────────────────────────────────
    List<User> findByStatus(UserStatus status);           // demandes en attente
    List<User> findByRole(Role role);                     // tous les organisateurs
    long countByRole(Role role);                          // stats
    long countByStatus(UserStatus status);                // stats
}