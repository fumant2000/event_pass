package com.eventpass.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ValidateTicketRequest {
    @NotBlank private String qrCode;
}
