package com.eventpass.backend.dto.response;

import com.eventpass.backend.enums.TransactionType;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private Double amount;
    private TransactionType type;
    private String reference;
    private String description;
    private LocalDateTime createdAt;
    private Long ticketId;
    private String eventTitle;
}