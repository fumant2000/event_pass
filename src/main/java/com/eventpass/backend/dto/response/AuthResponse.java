package com.eventpass.backend.dto.response;

import com.eventpass.backend.enums.Role;
import com.eventpass.backend.enums.UserStatus;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;

    @Builder.Default
    private String type = "Bearer";

    private Long userId;
    private String name;
    private String email;
    private Role role;
    private UserStatus status;
}