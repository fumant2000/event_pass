package com.eventpass.backend.controller;

import com.eventpass.backend.dto.request.BuyTicketRequest;
import com.eventpass.backend.dto.request.ValidateTicketRequest;
import com.eventpass.backend.dto.response.MessageResponse;
import com.eventpass.backend.dto.response.TicketResponse;
import com.eventpass.backend.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/buy")
    public ResponseEntity<TicketResponse> buyTicket(
            @Valid @RequestBody BuyTicketRequest req, Principal principal) {
        return ResponseEntity.ok(ticketService.buyTicket(req.getEventId(), principal.getName()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<TicketResponse>> getMyTickets(Principal principal) {
        return ResponseEntity.ok(ticketService.getMyTickets(principal.getName()));
    }

    @PostMapping("/validate")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> validateTicket(
            @Valid @RequestBody ValidateTicketRequest req, Principal principal) {
        return ResponseEntity.ok(ticketService.validateTicket(req.getQrCode(), principal.getName()));
    }

    @DeleteMapping("/{ticketId}")
    public ResponseEntity<MessageResponse> cancelTicket(
            @PathVariable Long ticketId, Principal principal) {
        return ResponseEntity.ok(ticketService.cancelMyTicket(ticketId, principal.getName()));
    }
}
