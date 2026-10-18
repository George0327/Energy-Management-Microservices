package com.example.demo.repository;

import com.example.demo.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    
    List<Message> findByUserIdOrderByTimestampAsc(UUID userId);
    
    @Query("SELECT m.userId FROM Message m GROUP BY m.userId ORDER BY MAX(m.timestamp) DESC")
    List<UUID> findDistinctUserIds();
    
    @Query("SELECT m FROM Message m WHERE m.userId = :userId ORDER BY m.timestamp DESC LIMIT 1")
    Message findLastMessageByUserId(UUID userId);
}
