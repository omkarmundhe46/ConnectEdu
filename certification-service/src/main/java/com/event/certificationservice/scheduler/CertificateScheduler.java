package com.event.certificationservice.scheduler;

import com.event.certificationservice.client.EventClient;
import com.event.certificationservice.dto.EventResponseDto;
import com.event.certificationservice.dto.ParticipantResponseDto;
import com.event.certificationservice.service.CertificateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CertificateScheduler {

    private final EventClient eventClient;
    private final CertificateService certificateService;

    /**
     * Runs every day at 7:00 PM (19:00).
     * Finds today's completed events and sends certificates to all participants.
     */
    @Scheduled(cron = "0 0 19 * * ?")
    public void distributeCertificatesForCompletedEvents() {
        log.info("Starting scheduled job: Distribute Certificates for Today's Events.");
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        try {
            List<EventResponseDto> todaysEvents = eventClient.getEventsByDate(today);
            if (todaysEvents.isEmpty()) {
                log.info("No events found for today. Job finished.");
                return;
            }

            log.info("Found {} events for today. Processing participants.", todaysEvents.size());

            for (EventResponseDto event : todaysEvents) {
                try {
                    List<ParticipantResponseDto> participants = eventClient.getEventParticipants(event.getId());
                    log.info("Processing {} participants for event: '{}'", participants.size(), event.getName());

                    for (ParticipantResponseDto participant : participants) {
                        certificateService.generateAndNotifyParticipant(event.getId(), participant.getUserId());
                    }
                } catch (Exception e) {
                    log.error("Failed to process participants for event ID {}. Error: {}", event.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("The certificate distribution job failed entirely. Error: {}", e.getMessage());
        }
        log.info("Scheduled certificate distribution job finished.");
    }
}