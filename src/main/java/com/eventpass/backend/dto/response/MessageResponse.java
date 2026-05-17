package com.eventpass.backend.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {
    private String message;
    private boolean success;

    public static MessageResponse ok(String message) {
        return new MessageResponse(message, true);
    }

    public static MessageResponse error(String message) {
        return new MessageResponse(message, false);
    }
}