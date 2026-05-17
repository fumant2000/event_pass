package com.eventpass.backend.dto.response;

import com.eventpass.backend.enums.TicketStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
    private Long id;
    private String qrCode;
    private Long eventId;
    private String eventTitle;
    private String eventLocation;
    private LocalDateTime eventDate;
    private Double amount;
    private TicketStatus status;
    private LocalDateTime purchasedAt;
    private LocalDateTime usedAt;
}