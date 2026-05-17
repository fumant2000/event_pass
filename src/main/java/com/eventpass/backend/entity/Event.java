package com.eventpass.backend.entity;

import com.eventpass.backend.enums.EventStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private String location;
    private String category;
    private String imageUrl;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    private Integer capacity;
    private Integer availableSeats;
    private Double price;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EventStatus status = EventStatus.PUBLISHED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
}
