package com.campusconnect.discussionservice.scheduler;

import com.campusconnect.discussionservice.client.EventClient;
import com.campusconnect.discussionservice.dto.EventDto;
import com.campusconnect.discussionservice.repository.DiscussionMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class DiscussionCleanupScheduler {

    private final EventClient eventClient;
    private final DiscussionMessageRepository messageRepository;

    /**
     * Runs every day at 2:00 AM.
     * Finds events that ended 2 days ago and deletes their discussion messages.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldDiscussions() {
        LocalDate cutoffDate = LocalDate.now().minusDays(2);
        log.info("Starting discussion cleanup job for events that ended on or before: {}", cutoffDate);

        try {
            List<EventDto> oldEvents = eventClient.getEventsEndedBefore(cutoffDate.toString());
            if (oldEvents.isEmpty()) {
                log.info("No old events found for discussion cleanup.");
                return;
            }

            List<Long> eventIdsToDelete = oldEvents.stream().map(EventDto::getId).collect(Collectors.toList());
            log.info("Found {} events for cleanup. Deleting messages for event IDs: {}", eventIdsToDelete.size(), eventIdsToDelete);

            messageRepository.deleteByEventIdIn(eventIdsToDelete);
            log.info("Successfully deleted messages for {} events.", eventIdsToDelete.size());

        } catch (Exception e) {
            log.error("Discussion cleanup job failed: {}", e.getMessage());
        }
    }
}