package com.eventpass.backend.repository;

import com.eventpass.backend.entity.Event;
import com.eventpass.backend.entity.Ticket;
import com.eventpass.backend.entity.User;
import com.eventpass.backend.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByQrCode(String qrCode);
    List<Ticket> findByUserOrderByPurchasedAtDesc(User user);
    List<Ticket> findByEventAndStatus(Event event, TicketStatus status);
}