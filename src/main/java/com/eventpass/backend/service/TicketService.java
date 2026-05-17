package com.eventpass.backend.service;

import com.eventpass.backend.dto.response.MessageResponse;
import com.eventpass.backend.dto.response.TicketResponse;
import com.eventpass.backend.entity.*;
import com.eventpass.backend.enums.*;
import com.eventpass.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public TicketResponse buyTicket(Long eventId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Événement introuvable"));

        if (event.getStatus() != EventStatus.PUBLISHED)
            throw new RuntimeException("Cet événement n'est pas disponible");
        if (event.getAvailableSeats() <= 0)
            throw new RuntimeException("Plus de places disponibles");
        if (event.getEventDate().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Cet événement est passé");

        event.setAvailableSeats(event.getAvailableSeats() - 1);
        eventRepository.save(event);

        Ticket ticket = ticketRepository.save(Ticket.builder()
                .qrCode(UUID.randomUUID().toString())
                .user(user).event(event)
                .amount(event.getPrice())
                .build());

        saveTransaction(user, ticket, TransactionType.PURCHASE,
                "Achat ticket : " + event.getTitle());

        return toResponse(ticket);
    }

    public List<TicketResponse> getMyTickets(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        return ticketRepository.findByUserOrderByPurchasedAtDesc(user)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public MessageResponse validateTicket(String qrCode, String organizerEmail) {
        Ticket ticket = ticketRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new RuntimeException("QR code invalide"));

        if (!ticket.getEvent().getOrganizer().getEmail().equals(organizerEmail))
            throw new RuntimeException("Vous n'êtes pas l'organisateur de cet événement");

        return switch (ticket.getStatus()) {
            case USED -> MessageResponse.error("Ticket déjà utilisé le " + ticket.getUsedAt());
            case REFUNDED -> MessageResponse.error("Ticket remboursé");
            case VALID -> {
                ticket.setStatus(TicketStatus.USED);
                ticket.setUsedAt(LocalDateTime.now());
                ticketRepository.save(ticket);
                yield MessageResponse.ok("✅ Entrée validée — Bienvenue " + ticket.getUser().getName());
            }
        };
    }

    @Transactional
    public void refundTicket(Ticket ticket) {
        ticket.setStatus(TicketStatus.REFUNDED);
        ticketRepository.save(ticket);

        Event event = ticket.getEvent();
        event.setAvailableSeats(event.getAvailableSeats() + 1);
        eventRepository.save(event);

        saveTransaction(ticket.getUser(), ticket, TransactionType.REFUND,
                "Remboursement : " + event.getTitle() + " (annulé)");
    }

    private void saveTransaction(User user, Ticket ticket, TransactionType type, String desc) {
        transactionRepository.save(Transaction.builder()
                .user(user).ticket(ticket).amount(ticket.getAmount()).type(type)
                .reference(type.name().substring(0, 3) + "-" +
                        UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .description(desc)
                .build());
    }

    public TicketResponse toResponse(Ticket t) {
        return TicketResponse.builder()
                .id(t.getId()).qrCode(t.getQrCode())
                .eventId(t.getEvent().getId()).eventTitle(t.getEvent().getTitle())
                .eventLocation(t.getEvent().getLocation()).eventDate(t.getEvent().getEventDate())
                .amount(t.getAmount()).status(t.getStatus())
                .purchasedAt(t.getPurchasedAt()).usedAt(t.getUsedAt())
                .build();
    }

    @Transactional
    public MessageResponse cancelMyTicket(Long ticketId, String userEmail) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket introuvable"));


        if (!ticket.getUser().getEmail().equals(userEmail))
            throw new RuntimeException("Ce ticket ne vous appartient pas");

        if (ticket.getStatus() != TicketStatus.VALID)
            return MessageResponse.error("Ce ticket ne peut pas être annulé (statut : " + ticket.getStatus() + ")");

        if (ticket.getEvent().getEventDate().isBefore(LocalDateTime.now()))
            return MessageResponse.error("L'événement est déjà passé");

        refundTicket(ticket); 
        return MessageResponse.ok("Ticket annulé et remboursé");
    }
}