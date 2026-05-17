package com.eventpass.backend.controller;

import com.eventpass.backend.dto.request.EventRequest;
import com.eventpass.backend.dto.response.EventResponse;
import com.eventpass.backend.dto.response.MessageResponse;
import com.eventpass.backend.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllPublishedEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody EventRequest req, Principal principal) {
        return ResponseEntity.ok(eventService.createEvent(req, principal.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id, @Valid @RequestBody EventRequest req, Principal principal) {
        return ResponseEntity.ok(eventService.updateEvent(id, req, principal.getName()));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> cancelEvent(
            @PathVariable Long id, Principal principal) {
        eventService.cancelEventAndRefund(id, principal.getName());
        return ResponseEntity.ok(MessageResponse.ok("Événement annulé, remboursements en cours"));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<List<EventResponse>> getMyEvents(Principal principal) {
        return ResponseEntity.ok(eventService.getMyEvents(principal.getName()));
    }
}
