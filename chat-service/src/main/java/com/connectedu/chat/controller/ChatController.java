package com.connectedu.chat.controller;

import com.connectedu.chat.dto.ChatRequest;
import com.connectedu.chat.dto.ChatResponse;
import com.connectedu.chat.entity.ChatMessage;
import com.connectedu.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/ask")
    public ResponseEntity<ChatResponse> ask(@RequestBody ChatRequest request) {
        Long userId = getAuthenticatedUserId();
        return ResponseEntity.ok(chatService.process(request.getMessage(), userId));
    }

    private Long getAuthenticatedUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Long) {
                return (Long) auth.getPrincipal(); // Logic matches JwtAuthenticationFilter
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getChatHistory() {
        Long userId = getAuthenticatedUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(chatService.getHistory(userId));
    }
}