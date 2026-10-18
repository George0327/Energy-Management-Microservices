package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private UUID userId;      // The client user (conversation owner)
    private UUID senderId;    // Who is sending (null = userId for user messages)
    private String senderType; // "USER" or "ADMIN" (null defaults to "USER")
    private String message;
}
