package com.eventpass.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EventRequest {
    @NotBlank private String title;
    private String description;
    private String location;
    private String category;
    private String imageUrl;
    @NotNull @Future private LocalDateTime eventDate;
    @NotNull @Min(1) private Integer capacity;
    @NotNull @Min(0) private Double price;
}


