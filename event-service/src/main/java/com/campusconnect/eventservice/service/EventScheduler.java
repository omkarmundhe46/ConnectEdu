package com.campusconnect.eventservice.service;

import com.campusconnect.eventservice.entity.Event;
import com.campusconnect.eventservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventScheduler {

    private final EventRepository eventRepository;
    private final EventService eventService;

    private static final LocalTime CUTOFF_TIME = LocalTime.of(23, 58); 

    /**
     * Runs every 2 minutes.
     * - Completes today's events if time >= 10:17 PM.
     * - Completes past events that were missed.
     */
    @Scheduled(fixedRate = 3600000)
    public void processCompletedEvents() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        log.info("🔄 Checking for events to complete at {}", now);

        List<Event> events = eventRepository.findByDateLessThanEqualAndCompletedFalse(now);
        log.info("Found {} events to process", events.size());

        for (Event event : events) {
            LocalDate eventDate = event.getDate().toLocalDate();

            // Case 1: Today’s event after cutoff
            if (eventDate.isEqual(today) && now.toLocalTime().isAfter(CUTOFF_TIME)) {
                log.info("✅ Completing today's event (after {}): {}", CUTOFF_TIME, event.getName());
                event.setCompleted(true);
                eventRepository.save(event);
                eventService.completeEventAndSendCertificates(event.getId());
            }

            // Case 2: Past event (missed previously)
            else if (eventDate.isBefore(today)) {
                log.info("⚠️ Missed event, marking complete: {}", event.getName());
                event.setCompleted(true);
                eventRepository.save(event);
                eventService.completeEventAndSendCertificates(event.getId());
            }
        }
    }
}
