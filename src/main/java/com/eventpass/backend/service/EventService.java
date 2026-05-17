package com.eventpass.backend.service;

import com.eventpass.backend.dto.request.EventRequest;
import com.eventpass.backend.dto.response.EventResponse;
import com.eventpass.backend.entity.Event;
import com.eventpass.backend.entity.Ticket;
import com.eventpass.backend.entity.User;
import com.eventpass.backend.enums.EventStatus;
import com.eventpass.backend.enums.TicketStatus;
import com.eventpass.backend.repository.EventRepository;
import com.eventpass.backend.repository.TicketRepository;
import com.eventpass.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final TicketService ticketService;

    public List<EventResponse> getAllPublishedEvents() {
        return eventRepository.findByStatus(EventStatus.PUBLISHED)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public EventResponse getEventById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public EventResponse createEvent(EventRequest req, String organizerEmail) {
        User organizer = userRepository.findByEmail(organizerEmail)
                .orElseThrow(() -> new RuntimeException("Organisateur introuvable"));

        Event event = eventRepository.save(Event.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .location(req.getLocation())
                .category(req.getCategory())
                .imageUrl(req.getImageUrl())
                .eventDate(req.getEventDate())
                .capacity(req.getCapacity())
                .availableSeats(req.getCapacity())
                .price(req.getPrice())
                .organizer(organizer)
                .build());

        return toResponse(event);
    }

    public EventResponse updateEvent(Long id, EventRequest req, String organizerEmail) {
        Event event = findOrThrow(id);
        checkOwner(event, organizerEmail);

        if (event.getStatus() == EventStatus.CANCELLED)
            throw new RuntimeException("Impossible de modifier un événement annulé");

        int soldTickets = event.getCapacity() - event.getAvailableSeats();
        if (req.getCapacity() < soldTickets)
            throw new RuntimeException("La capacité ne peut pas être inférieure aux tickets vendus (" + soldTickets + ")");

        event.setTitle(req.getTitle());
        event.setDescription(req.getDescription());
        event.setLocation(req.getLocation());
        event.setCategory(req.getCategory());
        event.setImageUrl(req.getImageUrl());
        event.setEventDate(req.getEventDate());
        event.setPrice(req.getPrice());
        event.setCapacity(req.getCapacity());
        event.setAvailableSeats(req.getCapacity() - soldTickets);
        event.setUpdatedAt(LocalDateTime.now());

        return toResponse(eventRepository.save(event));
    }

     @Transactional
    public void cancelEventAndRefund(Long id, String organizerEmail) {
        Event event = findOrThrow(id);
        checkOwner(event, organizerEmail);

        if (event.getStatus() == EventStatus.CANCELLED)
            throw new RuntimeException("Événement déjà annulé");

        event.setStatus(EventStatus.CANCELLED);
        event.setUpdatedAt(LocalDateTime.now());
        eventRepository.save(event);

        // Remboursement automatique de tous les tickets valides
        List<Ticket> tickets = ticketRepository.findByEventAndStatus(event, TicketStatus.VALID);
        
        // Utilisation d'une boucle classique pour éviter le conflit de Consumer
        for (Ticket ticket : tickets) {
            ticketService.refundTicket(ticket);
        }
    }

    public List<EventResponse> getMyEvents(String organizerEmail) {
        User organizer = userRepository.findByEmail(organizerEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        return eventRepository.findByOrganizer(organizer)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ─── helpers ────────────────────────────────────────────────────────────

    private Event findOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement introuvable"));
    }

    private void checkOwner(Event event, String email) {
        if (!event.getOrganizer().getEmail().equals(email))
            throw new RuntimeException("Accès refusé : vous n'êtes pas l'organisateur");
    }

    public EventResponse toResponse(Event e) {
        return EventResponse.builder()
                .id(e.getId()).title(e.getTitle()).description(e.getDescription())
                .location(e.getLocation()).category(e.getCategory()).imageUrl(e.getImageUrl())
                .eventDate(e.getEventDate()).capacity(e.getCapacity())
                .availableSeats(e.getAvailableSeats()).price(e.getPrice())
                .status(e.getStatus()).organizerName(e.getOrganizer().getName())
                .organizerId(e.getOrganizer().getId()).createdAt(e.getCreatedAt())
                .build();
    }
}
