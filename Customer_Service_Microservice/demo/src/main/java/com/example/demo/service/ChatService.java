package com.example.demo.service;

import com.example.demo.dto.ChatEventDTO;
import com.example.demo.dto.ChatMessageDTO;
import com.example.demo.dto.MessageDTO;
import com.example.demo.entity.Message;
import com.example.demo.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final MessageRepository messageRepository;
    private final ChatEventPublisher chatEventPublisher;

    @Value("${app.admin.id}")
    private String staticAdminId;

    public ChatService(MessageRepository messageRepository, ChatEventPublisher chatEventPublisher) {
        this.messageRepository = messageRepository;
        this.chatEventPublisher = chatEventPublisher;
    }

    /**
     * Send a message (from user or admin)
     */
    public MessageDTO sendMessage(ChatMessageDTO request) {
        Message message = new Message();
        message.setUserId(request.getUserId());
        message.setSenderId(request.getSenderId() != null ? request.getSenderId() : request.getUserId());
        message.setSenderType(request.getSenderType() != null ? request.getSenderType() : "USER");
        message.setContent(request.getMessage());
        message.setTimestamp(LocalDateTime.now());

        message = messageRepository.save(message);

        // Publish event for real-time delivery
        ChatEventDTO event = new ChatEventDTO(
                message.getId(),
                message.getUserId(),
                message.getSenderId(),
                message.getSenderType(),
                message.getContent(),
                message.getTimestamp()
        );
        chatEventPublisher.publishChatEvent(event);

        return toDTO(message);
    }

    /**
     * Get all messages for a user (conversation history)
     */
    public List<MessageDTO> getMessagesByUserId(UUID userId) {
        return messageRepository.findByUserIdOrderByTimestampAsc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get list of users who have chat messages (for admin)
     */
    public List<UUID> getActiveUsers() {
        return messageRepository.findDistinctUserIds();
    }

    /**
     * Get admin ID
     */
    public UUID getAdminId() {
        return UUID.fromString(staticAdminId);
    }

    private MessageDTO toDTO(Message message) {
        return new MessageDTO(
                message.getId(),
                message.getUserId(),
                message.getSenderId(),
                message.getSenderType(),
                message.getContent(),
                message.getTimestamp()
        );
    }
}
