package com.campusconnect.eventservice.service;

import com.campusconnect.eventservice.client.DiscussionClient;
import com.campusconnect.eventservice.entity.Event;
import com.campusconnect.eventservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiscussionCleanupService {

    private final EventRepository eventRepository;
    private final DiscussionClient discussionClient;

    /**
     * Runs every day at 3:00 AM.
     * Finds events that ended 7 days ago and deletes their chat history.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOldDiscussions() {
        log.info("Running scheduled job: CleanupOldDiscussions");

        // 1. Calculate the cutoff date (7 days ago)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // 2. Find events that ended before this cutoff and are marked as completed
        List<Event> eventsToClean = eventRepository.findByDateBeforeAndCompletedTrue(sevenDaysAgo);

        if (eventsToClean.isEmpty()) {
            log.info("No old event discussions to clean up.");
            return;
        }

        log.info("Found {} event discussions to clean up.", eventsToClean.size());
        int successCount = 0;

        // 3. For each event, call the discussion-service to delete the chat
        for (Event event : eventsToClean) {
            try {
                discussionClient.deleteChatHistory(event.getId());
                log.info("Successfully deleted chat history for eventId: {}", event.getId());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to delete chat history for eventId: {}. Error: {}", event.getId(), e.getMessage());
            }
        }
        log.info("Cleanup job finished. Successfully deleted {} of {} discussion histories.", successCount, eventsToClean.size());
    }
}