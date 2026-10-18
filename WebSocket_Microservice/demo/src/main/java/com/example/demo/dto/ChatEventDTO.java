package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatEventDTO {
    private UUID messageId;
    private UUID userId;
    private UUID senderId;
    private String senderType; // "USER" or "ADMIN"
    private String content;
    private LocalDateTime timestamp;
}
