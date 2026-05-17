package com.eventpass.backend.dto.response;

import com.eventpass.backend.enums.EventStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private String location;
    private String category;
    private String imageUrl;
    private LocalDateTime eventDate;
    private Integer capacity;
    private Integer availableSeats;
    private Double price;
    private EventStatus status;
    private String organizerName;
    private Long organizerId;
    private LocalDateTime createdAt;
}