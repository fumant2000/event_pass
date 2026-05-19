package com.eventpass.backend.dto.response;

import com.eventpass.backend.enums.UserStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizerRequestResponse {
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private UserStatus status;
    private LocalDateTime createdAt;
}