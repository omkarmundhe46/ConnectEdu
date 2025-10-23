package com.campusconnect.eventservice.service;

import com.campusconnect.eventservice.client.PaymentClient;
import com.campusconnect.eventservice.dto.*;
import com.campusconnect.eventservice.entity.Event;
import com.campusconnect.eventservice.entity.EventParticipant;
import com.campusconnect.eventservice.exception.*;
import com.campusconnect.eventservice.kafka.EventKafkaProducer;
import com.campusconnect.eventservice.repository.EventRepository;
import com.campusconnect.eventservice.repository.EventParticipantRepository;
import com.campusconnect.eventservice.client.CertificateClient;
import com.campusconnect.eventservice.client.ClubClient;
import com.campusconnect.eventservice.client.UserClient;
import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final EventParticipantRepository participantRepository;
    private final ClubClient clubClient;
    private final EventKafkaProducer eventKafkaProducer;
    private final UserClient userClient;
    private final PaymentClient paymentClient; // Inject the new client
    private final CertificateClient certificateClient;

    public EventResponseDto createClubEvent(Long clubId, EventRequestDto eventRequestDto) {
        // Validate club exists
        try {
            clubClient.getClubById(clubId);
        } catch (FeignException.NotFound e) {
            throw new ClubNotFoundException("Club not found with id: " + clubId);
        }

        Event event = new Event();
        event.setName(eventRequestDto.getName());
        event.setDescription(eventRequestDto.getDescription());
        event.setDate(eventRequestDto.getDate());
        event.setLocation(eventRequestDto.getLocation());
        event.setClubId(clubId);
        event.setImageUrl(eventRequestDto.getImageUrl()); // Set the image URL
        event.setMeetingLink(eventRequestDto.getMeetingLink()); // Set meeting link too

        Event savedEvent = eventRepository.save(event);
        return mapToEventResponseDto(savedEvent);
    }

    // The confusing gatherAndSendEventCreatedNotification method has been removed.
    // All other methods below are correct and do not need any changes.

    public List<EventResponseDto> getClubEvents(Long clubId) {
        return eventRepository.findByClubId(clubId).stream()
                .map(this::mapToEventResponseDto)
                .collect(Collectors.toList());
    }

    public EventResponseDto getClubEventById(Long clubId, Long eventId) {
        Event event = eventRepository.findByIdAndClubId(eventId, clubId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId + " for club: " + clubId));
        return mapToEventResponseDto(event);
    }

    public EventResponseDto updateClubEvent(Long clubId, Long eventId, EventRequestDto eventRequestDto) {
        Event event = eventRepository.findByIdAndClubId(eventId, clubId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId + " for club: " + clubId));

        event.setName(eventRequestDto.getName());
        event.setDescription(eventRequestDto.getDescription());
        event.setDate(eventRequestDto.getDate());
        event.setLocation(eventRequestDto.getLocation());
        event.setImageUrl(eventRequestDto.getImageUrl()); // Update the image URL
        event.setMeetingLink(eventRequestDto.getMeetingLink()); // Update meeting link
        event.setUpdatedAt(LocalDateTime.now());

        Event updatedEvent = eventRepository.save(event);
        return mapToEventResponseDto(updatedEvent);
    }

    public void deleteClubEvent(Long clubId, Long eventId) {
        Event event = eventRepository.findByIdAndClubId(eventId, clubId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId + " for club: " + clubId));
        eventRepository.delete(event);
    }

//    public ParticipantResponseDto addParticipantToEvent(Long clubId, Long eventId, EventParticipationDTO participantRequestDto) {
//        // Step 1: Get the authenticated user's details from the JWT
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        Jwt jwt = (Jwt) authentication.getPrincipal();
//        // --- THIS IS THE FIX ---
//        // Safely get the userId claim and convert it from Integer to Long.
//        Object userIdObj = jwt.getClaim("userId");
//        Long authenticatedUserId = null;
//        if (userIdObj instanceof Number) {
//            authenticatedUserId = ((Number) userIdObj).longValue();
//        }
//        // --- END OF FIX ---
//
//        String userRole = jwt.getClaimAsStringList("roles").get(0);
//
//        // This check will now work correctly without a NullPointerException
//        if (authenticatedUserId == null || !authenticatedUserId.equals(participantRequestDto.getUserId())) {
//            throw new AccessDeniedException("You can only register yourself for an event.");
//        }
//
//        // Step 2: Check the business rule for Club Members
//        if ("ROLE_CLUB_MEMBER".equals(userRole)) {
//            log.info("User is a CLUB_MEMBER. Checking if they belong to this club...");
//            // Make an authenticated call to club-service
//            boolean isMemberOfThisClub = clubClient.isMember(clubId, authenticatedUserId);
//
//            if (isMemberOfThisClub) {
//                log.warn("Participation denied for user {} in their own club's event (clubId: {})", authenticatedUserId, clubId);
//                throw new ParticipationDeniedException("Club members cannot participate in their own club's events.");
//            }
//        }
//
//        // Step 3: Proceed with the existing logic if the checks pass
//        Event event = eventRepository.findByIdAndClubId(eventId, clubId)
//                .orElseThrow(() -> new EventNotFoundException("Event not found"));
//
//        if (participantRepository.existsByEventIdAndUserId(eventId, participantRequestDto.getUserId())) {
//            throw new DuplicateParticipationException("User already participating in this event");
//        }
//
//        EventParticipant participant = new EventParticipant();
//        participant.setEventId(eventId);
//        participant.setUserId(participantRequestDto.getUserId());
//
//        EventParticipant savedParticipant = participantRepository.save(participant);
//        log.info("Participant saved: {}", savedParticipant);
//
//        participantRequestDto.setEventId(eventId);
//        eventKafkaProducer.sendEventParticipationNotification(participantRequestDto);
//        log.info("Event participation notification queued for user: {}", participantRequestDto.getUserId(), eventId);
//
//        return mapToParticipantResponseDto(savedParticipant);
//    }

    // --- ADD THIS NEW METHOD ---
    public OrderResponse startRegistration(Long clubId, Long eventId, RegistrationRequestDto request) {
        // 1. Validate the event exists and belongs to the club.
        eventRepository.findByIdAndClubId(eventId, clubId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        // 2. Check if the user is already registered to prevent double payment.
        if (participantRepository.existsByEventIdAndUserId(eventId, request.getUserId())) {
            throw new DuplicateParticipationException("You are already registered for this event.");
        }

        // 3. Prepare the request for the payment-service.
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setAmount(request.getAmount());
        orderRequest.setCurrency("INR");

        log.info("Requesting payment order creation for event {} and user {}", eventId, request.getUserId());

        // 4. Call the payment-service to create a Razorpay order.
        return paymentClient.createOrder(orderRequest);
    }

    public List<ParticipantResponseDto> getEventParticipants(Long clubId, Long eventId) {
        Event event = eventRepository.findByIdAndClubId(eventId, clubId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId + " for club: " + clubId));

        return participantRepository.findByEventId(eventId).stream()
                .map(this::mapToParticipantResponseDto)
                .collect(Collectors.toList());
    }

    public void removeParticipantFromEvent(Long clubId, Long eventId, Long userId) {
        Event event = eventRepository.findByIdAndClubId(eventId, clubId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId + " for club: " + clubId));

        EventParticipant participant = participantRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new ParticipantNotFoundException("Participant not found for event: " + eventId + " and user: " + userId));

        participantRepository.delete(participant);
    }

    public EventResponseDto getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));
        return mapToEventResponseDto(event);
    }

    private EventResponseDto mapToEventResponseDto(Event event) {
        EventResponseDto dto = new EventResponseDto();
        dto.setId(event.getId());
        dto.setName(event.getName());
        dto.setDescription(event.getDescription());
        dto.setDate(event.getDate());
        dto.setLocation(event.getLocation());
        dto.setClubId(event.getClubId());
        dto.setCreatedAt(event.getCreatedAt());
        dto.setUpdatedAt(event.getUpdatedAt());
        dto.setMeetingLink(event.getMeetingLink());
        dto.setImageUrl(event.getImageUrl()); // Map the image URL
        // Calculate the status based on the event date
        if (event.getDate() != null && event.getDate().isAfter(LocalDateTime.now())) {
            dto.setStatus("UPCOMING");
        } else {
            dto.setStatus("COMPLETED");
        }
        return dto;
    }

    private ParticipantResponseDto mapToParticipantResponseDto(EventParticipant participant) {
        ParticipantResponseDto dto = new ParticipantResponseDto();
        dto.setId(participant.getId());
        dto.setUserId(participant.getUserId());
        dto.setEventId(participant.getEventId());
        dto.setRegisteredAt(participant.getRegisteredAt());
        return dto;
    }

    @Transactional
    public void completeEventAndSendCertificates(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id " + eventId));

        event.setCompleted(true);
        eventRepository.save(event);
        if (event.getCertificatesGenerated()) {
            throw new RuntimeException("Certificates already generated for this event");
        }

        for (EventParticipant participant : event.getParticipants()) {
            certificateClient.generateAndSend(event.getId(), participant.getUserId());
        }

        event.setCertificatesGenerated(true);
        eventRepository.save(event);
    }

    // ADD THIS NEW METHOD: For the discussion service's cleanup scheduler
    public List<EventResponseDto> findEventsEndedBefore(LocalDate date) {
        LocalDateTime dateTime = date.atStartOfDay();
        return eventRepository.findByDateBefore(dateTime).stream()
                .map(this::mapToEventResponseDto)
                .collect(Collectors.toList());
    }

    // ADD THIS NEW METHOD
    public EventResponseDto updateMeetingLink(Long clubId, Long eventId, String meetingLink) {
        Event event = eventRepository.findByIdAndClubId(eventId, clubId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        event.setMeetingLink(meetingLink);
        Event updatedEvent = eventRepository.save(event);
        return mapToEventResponseDto(updatedEvent);
    }
    // ADD THIS NEW METHOD: Business logic to find events by date.
    public List<EventResponseDto> findEventsByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        return eventRepository.findByDateBetween(startOfDay, endOfDay).stream()
                .map(this::mapToEventResponseDto)
                .collect(Collectors.toList());
    }

    // ADD THIS METHOD
    public List<EventResponseDto> getAllUpcomingEvents() {
        return eventRepository.findByDateAfterOrderByDateAsc(LocalDateTime.now())
                .stream()
                .map(this::mapToEventResponseDto)
                .collect(Collectors.toList());
    }

}