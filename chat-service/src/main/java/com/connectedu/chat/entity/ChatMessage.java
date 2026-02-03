package com.connectedu.chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId; // The user who owns this chat

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private String sender; // "USER" or "BOT"

    @CreationTimestamp // Automatically sets time when saved
    @Column(updatable = false)
    private LocalDateTime createdAt;
}