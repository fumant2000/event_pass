package com.eventpass.backend.entity;

import com.eventpass.backend.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private String reference; // ex: "TXN-ABC12345"
    private String description;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
