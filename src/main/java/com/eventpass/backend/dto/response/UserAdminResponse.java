package com.eventpass.backend.dto.response;


import com.eventpass.backend.enums.Role;
import com.eventpass.backend.enums.UserStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserAdminResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private UserStatus status;
    private LocalDateTime createdAt;
}
