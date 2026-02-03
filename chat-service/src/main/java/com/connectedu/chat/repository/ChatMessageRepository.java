package com.connectedu.chat.repository;

import com.connectedu.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Fetch chat history for a specific user
    List<ChatMessage> findByUserIdOrderByCreatedAtAsc(Long userId);

    // DELETE command for cleanup (Native Query for speed or JPA)
    @Modifying
    @Transactional
    @Query("DELETE FROM ChatMessage c WHERE c.createdAt < :cutoffDate")
    void deleteMessagesOlderThan(LocalDateTime cutoffDate);
}