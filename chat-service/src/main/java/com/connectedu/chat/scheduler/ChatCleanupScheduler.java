package com.connectedu.chat.scheduler;

import com.connectedu.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatCleanupScheduler {

    private final ChatMessageRepository chatRepository;

    // Runs every day at midnight (00:00:00)
    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteOldChatMessages() {
        // Calculate the date 7 days ago
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);

        log.info("🧹 Starting chat cleanup. Deleting messages older than: {}", cutoffDate);

        chatRepository.deleteMessagesOlderThan(cutoffDate);

        log.info("✅ Chat cleanup completed.");
    }
}