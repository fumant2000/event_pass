package com.eventpass.backend.repository;

import com.eventpass.backend.entity.Event;
import com.eventpass.backend.entity.Ticket;
import com.eventpass.backend.entity.User;
import com.eventpass.backend.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByQrCode(String qrCode);

    List<Ticket> findByUserOrderByPurchasedAtDesc(User user);

    List<Ticket> findByEventAndStatus(Event event, TicketStatus status);

    long countByStatus(TicketStatus status);

    // Pour le revenu total (somme des achats)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Ticket t WHERE t.status != 'REFUNDED'")
    long sumRevenue();
}