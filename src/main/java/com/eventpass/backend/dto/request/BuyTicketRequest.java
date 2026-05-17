package com.eventpass.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BuyTicketRequest {
    @NotNull private Long eventId;
}