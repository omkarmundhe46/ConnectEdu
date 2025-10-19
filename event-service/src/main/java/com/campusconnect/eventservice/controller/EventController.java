
package com.campusconnect.eventservice.controller;

import com.campusconnect.eventservice.client.CertificateClient;
import com.campusconnect.eventservice.client.ClubClient;
import com.campusconnect.eventservice.dto.*;
import com.campusconnect.eventservice.service.EventService;
import com.campusconnect.eventservice.kafka.EventKafkaProducer; // Import Kafka producer
//import com.campusconnect.eventservice.client.NotificationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;
    private final EventKafkaProducer eventKafkaProducer; // ADD THIS
//    private final NotificationClient notificationClient;
//    private ClubClient clubClient;
    // Inject the new CertificateClient
    private final CertificateClient certificateClient;

//    @PostMapping("/{clubId}/events")
//    public ResponseEntity<EventResponseDto> createClubEvent(@PathVariable Long clubId,
//                                                           @Valid @RequestBody EventRequestDto eventRequestDto) {
//        EventResponseDto createdEvent = eventService.createClubEvent(clubId, eventRequestDto);
//
//        // Send event created notification asynchronously
//        sendEventCreatedNotificationAsync(createdEvent);
//
//        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
//    }

    @PostMapping("/{clubId}/events")
    @PreAuthorize("hasAuthority('ROLE_CLUB_ADMIN')") // Only Club Admin can create events
    public ResponseEntity<EventResponseDto> createClubEvent(@PathVariable Long clubId,
                                                            @Valid @RequestBody EventRequestDto eventRequestDto) {
        validateClubOwnership(clubId); // Fine-grained check: Is this YOUR club?
        EventResponseDto createdEvent = eventService.createClubEvent(clubId, eventRequestDto);
        log.info("🔔 Queuing event created notification for eventId={}", createdEvent.getId());
        eventKafkaProducer.sendEventCreatedNotification(createdEvent);
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }


//    @Async
//    public CompletableFuture<Void> sendEventCreatedNotificationAsync(EventResponseDto event) {
//        try {
//            log.info("🔔 Sending event created notification for eventId={}, clubId={}", event.getId(), event.getClubId());
//
//            notificationClient.notifyEventCreated(event);
//
//            log.info("✅ Event created notification sent for event: {}", event.getId());
//        } catch (Exception e) {
//            log.error("❌ Failed to send event created notification for event: {}", event.getId(), e);
//        }
//        return CompletableFuture.completedFuture(null);
//    }

    @GetMapping("/{clubId}/events")
    @PreAuthorize("isAuthenticated()") // Any logged-in user can see events
    public ResponseEntity<List<EventResponseDto>> getClubEvents(@PathVariable Long clubId) {
        List<EventResponseDto> events = eventService.getClubEvents(clubId);
        return ResponseEntity.ok(events);
    }



    @PutMapping("/{clubId}/events/{eventId}")
    @PreAuthorize("hasAnyAuthority('ROLE_CLUB_ADMIN', 'ROLE_COLLEGE_ADMIN')")
    public ResponseEntity<EventResponseDto> updateClubEvent(@PathVariable Long clubId, @PathVariable Long eventId,
                                                            @Valid @RequestBody EventRequestDto eventRequestDto) {
        if (isClubAdmin()) {
            validateClubOwnership(clubId);
        }
        EventResponseDto updatedEvent = eventService.updateClubEvent(clubId, eventId, eventRequestDto);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{clubId}/events/{eventId}")
    @PreAuthorize("hasAnyAuthority('ROLE_CLUB_ADMIN', 'ROLE_COLLEGE_ADMIN')")
    public ResponseEntity<Void> deleteClubEvent(@PathVariable Long clubId, @PathVariable Long eventId) {
        if (isClubAdmin()) {
            validateClubOwnership(clubId);
        }
        eventService.deleteClubEvent(clubId, eventId);
        return ResponseEntity.noContent().build();
    }

//    @PostMapping("/{clubId}/events/{eventId}/participants")
//    @PreAuthorize("isAuthenticated()") // Any logged-in user can attempt to participate
//    public ResponseEntity<ParticipantResponseDto> addParticipantToEvent(@PathVariable Long clubId, @PathVariable Long eventId,
//                                                                        @Valid @RequestBody EventParticipationDTO participantRequestDto) {
//        // Business logic for who can participate should be inside the service layer
//        ParticipantResponseDto participant = eventService.addParticipantToEvent(clubId, eventId, participantRequestDto);
//        return new ResponseEntity<>(participant, HttpStatus.CREATED);
//    }

    @GetMapping("/{clubId}/events/{eventId}/participants")
    @PreAuthorize("hasAnyAuthority('ROLE_CLUB_ADMIN', 'ROLE_COLLEGE_ADMIN')")
    public ResponseEntity<List<ParticipantResponseDto>> getEventParticipants(@PathVariable Long clubId, @PathVariable Long eventId) {
        if (isClubAdmin()) {
            validateClubOwnership(clubId);
        }
        List<ParticipantResponseDto> participants = eventService.getEventParticipants(clubId, eventId);
        return ResponseEntity.ok(participants);
    }


    @GetMapping("/{clubId}/events/{eventId}/participants/{userId}/certificate/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadParticipantCertificate(
            @PathVariable Long clubId,
            @PathVariable Long eventId,
            @PathVariable Long userId) {

        // Fine-grained check: You can only download your own certificate
        Long authenticatedUserId = getAuthenticatedUserId();
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("You are not authorized to download this certificate.");
        }

        log.info("Request received to download certificate for event {} and user {}", eventId, userId);
        return certificateClient.downloadCertificate(eventId, userId);
    }







    //Not required Many more and semi public endpoints //
    @GetMapping("/{clubId}/events/{eventId}")
    public ResponseEntity<EventResponseDto> getClubEventById(@PathVariable Long clubId, @PathVariable Long eventId) {
        EventResponseDto event = eventService.getClubEventById(clubId, eventId);
        return ResponseEntity.ok(event);
    }

    @DeleteMapping("/{clubId}/events/{eventId}/participants/{userId}")
    public ResponseEntity<Void> removeParticipantFromEvent(@PathVariable Long clubId, @PathVariable Long eventId, @PathVariable Long userId) {
        eventService.removeParticipantFromEvent(clubId, eventId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/events/{eventId}/details")
    public ResponseEntity<EventResponseDto> getEventDetails(@PathVariable Long eventId) {
        EventResponseDto event = eventService.getEventById(eventId);
        return ResponseEntity.ok(event);
    }

    @PostMapping("/events/{eventId}/complete")
	public ResponseEntity<Void> completeEventAndSendCertificates(@PathVariable Long eventId) {
		eventService.completeEventAndSendCertificates(eventId);
		return ResponseEntity.ok().build();
	}

    // ADD THIS NEW ENDPOINT
    @PutMapping("/{clubId}/events/{eventId}/meeting-link")
    @PreAuthorize("hasAuthority('ROLE_CLUB_ADMIN')")
    public ResponseEntity<EventResponseDto> updateMeetingLink(
            @PathVariable Long clubId,
            @PathVariable Long eventId,
            @RequestBody String meetingLink) {

        validateClubOwnership(clubId); // Important security check!
        EventResponseDto updatedEvent = eventService.updateMeetingLink(clubId, eventId, meetingLink);
        return ResponseEntity.ok(updatedEvent);
    }


    // ... inside your EventController class's helper methods ...

    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();

        // --- APPLY THE SAME FIX HERE ---
        Object userIdObj = jwt.getClaim("userId");
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        // Return null or throw an exception if the claim is missing/invalid
        throw new IllegalStateException("User ID not found in token or is not a number.");
    }

    // ... inside your EventController class

    private void validateClubOwnership(Long clubId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();

        // APPLY THE SAME FIX HERE
        Object managedClubIdObj = jwt.getClaim("managedClubId");
        Long managedClubId = null;
        if (managedClubIdObj instanceof Number) {
            managedClubId = ((Number) managedClubIdObj).longValue();
        }

        if (managedClubId == null || !managedClubId.equals(clubId)) {
            throw new AccessDeniedException("You are not the admin of this club.");
        }
    }

    private boolean isClubAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_CLUB_ADMIN"));
    }

    // THIS IS THE NEW STARTING POINT FOR REGISTRATION
    @PostMapping("/{clubId}/events/{eventId}/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> startRegistration(
            @PathVariable Long clubId,
            @PathVariable Long eventId,
            @RequestBody RegistrationRequestDto request) {

        // We get the user ID from the token to prevent impersonation.
        request.setUserId(getAuthenticatedUserId());

        return ResponseEntity.ok(eventService.startRegistration(clubId, eventId, request));
    }


}
