package com.example.demo.controllers;

import com.example.demo.dto.ChatMessageDTO;
import com.example.demo.dto.MessageDTO;
import com.example.demo.service.ChatService;
import com.example.demo.service.RuleBasedChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/support")
public class ChatController {

    private final ChatService chatService;
    private final RuleBasedChatService ruleBasedChatService;

    public ChatController(ChatService chatService, RuleBasedChatService ruleBasedChatService) {
        this.chatService = chatService;
        this.ruleBasedChatService = ruleBasedChatService;
    }

    //MESSAGE ENDPOINTS

    /**
     * Send a message (user or admin)
     * User: { userId: "xxx", message: "hello" }
     * Admin: { userId: "xxx", senderId: "admin-uuid", senderType: "ADMIN", message: "hi" }
     */
    @PostMapping("/messages")
    public ResponseEntity<MessageDTO> sendMessage(@RequestBody ChatMessageDTO request) {
        MessageDTO response = chatService.sendMessage(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get messages for a user (admin viewing a user's chat)
     */
    @GetMapping("/messages/user/{userId}")
    public ResponseEntity<List<MessageDTO>> getMessagesForUser(@PathVariable UUID userId) {
        List<MessageDTO> messages = chatService.getMessagesByUserId(userId);
        return ResponseEntity.ok(messages);
    }

    /**
     * Get my messages (user viewing their own chat)
     */
    @GetMapping("/messages/my/{userId}")
    public ResponseEntity<List<MessageDTO>> getMyMessages(@PathVariable UUID userId) {
        List<MessageDTO> messages = chatService.getMessagesByUserId(userId);
        return ResponseEntity.ok(messages);
    }

    //ADMIN ENDPOINTS

    /**
     * Get list of users who have sent messages (for admin dashboard)
     */
    @GetMapping("/users/active")
    public ResponseEntity<List<UUID>> getActiveUsers() {
        List<UUID> userIds = chatService.getActiveUsers();
        return ResponseEntity.ok(userIds);
    }

    /**
     * Get the configured admin ID
     */
    @GetMapping("/admin/id")
    public ResponseEntity<UUID> getAdminId() {
        return ResponseEntity.ok(chatService.getAdminId());
    }

    //CHAT BOT

    /**
     * Chat with the rule-based bot
     */
    @PostMapping("/chat")
    public ResponseEntity<String> chatWithBot(@RequestBody ChatMessageDTO dto) {
        String response = ruleBasedChatService.handleMessage(dto.getMessage());
        
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.ok("I'm forwarding your message to our admin team. They'll respond shortly.");
        }
    }
}
