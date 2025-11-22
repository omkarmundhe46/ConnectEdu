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
import java.util.*;
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
        event.setContactName1(eventRequestDto.getContactName1());
        event.setContactPhone1(eventRequestDto.getContactPhone1());
        event.setContactName2(eventRequestDto.getContactName2());
        event.setContactPhone2(eventRequestDto.getContactPhone2());

        Event savedEvent = eventRepository.save(event);
        return mapToEventResponseDto(savedEvent);
    }

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
        event.setContactName1(eventRequestDto.getContactName1());
        event.setContactPhone1(eventRequestDto.getContactPhone1());
        event.setContactName2(eventRequestDto.getContactName2());
        event.setContactPhone2(eventRequestDto.getContactPhone2());

        Event updatedEvent = eventRepository.save(event);
        return mapToEventResponseDto(updatedEvent);
    }

    public List<EventResponseDto> searchEvents(String query) {
        return eventRepository.findByNameContainingIgnoreCase(query).stream()
                .map(this::mapToEventResponseDto)
                .collect(Collectors.toList());
    }

    public void deleteClubEvent(Long clubId, Long eventId) {
        Event event = eventRepository.findByIdAndClubId(eventId, clubId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId + " for club: " + clubId));
        eventRepository.delete(event);
    }

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
        dto.setContactName1(event.getContactName1());
        dto.setContactPhone1(event.getContactPhone1());
        dto.setContactName2(event.getContactName2());
        dto.setContactPhone2(event.getContactPhone2());
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

    public List<EventResponseDto> findEventsEndedBefore(LocalDate date) {
        LocalDateTime dateTime = date.atStartOfDay();
        return eventRepository.findByDateBefore(dateTime).stream()
                .map(this::mapToEventResponseDto)
                .collect(Collectors.toList());
    }

    public List<MyRegistrationResponseDto> getRegistrationsForUser(Long userId) {
        // 1. Get all participations for the user
        List<EventParticipant> participations = participantRepository.findByUserId(userId);
        if (participations.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Get all unique event IDs
        List<Long> eventIds = participations.stream()
                .map(EventParticipant::getEventId)
                .distinct()
                .collect(Collectors.toList());

        // 3. Fetch all corresponding events in one query
        Map<Long, Event> eventMap = eventRepository.findAllById(eventIds).stream()
                .collect(Collectors.toMap(Event::getId, event -> event));

        // 4. Combine the data
        return participations.stream()
                .map(p -> {
                    Event event = eventMap.get(p.getEventId());
                    if (event == null) return null; // Should not happen

                    String status = event.getDate() != null && event.getDate().isAfter(LocalDateTime.now()) ? "UPCOMING" : "COMPLETED";

                    return MyRegistrationResponseDto.builder()
                            .registeredAt(p.getRegisteredAt())
                            .paymentId(p.getPaymentId())
                            .eventId(event.getId())
                            .clubId(event.getClubId())
                            .eventName(event.getName())
                            .eventImageUrl(event.getImageUrl())
                            .eventDate(event.getDate())
                            .eventStatus(status)
                            .build();
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(MyRegistrationResponseDto::getEventDate).reversed()) // Show newest first
                .collect(Collectors.toList());
    }

    public boolean isUserRegistered(Long eventId, Long userId) {
        return participantRepository.existsByEventIdAndUserId(eventId, userId);
    }

    public EventResponseDto updateMeetingLink(Long clubId, Long eventId, String meetingLink) {
        Event event = eventRepository.findByIdAndClubId(eventId, clubId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        event.setMeetingLink(meetingLink);
        Event updatedEvent = eventRepository.save(event);
        return mapToEventResponseDto(updatedEvent);
    }
    public List<EventResponseDto> findEventsByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        return eventRepository.findByDateBetween(startOfDay, endOfDay).stream()
                .map(this::mapToEventResponseDto)
                .collect(Collectors.toList());
    }


    public List<EventResponseDto> getAllUpcomingEvents() {
        return eventRepository.findByDateAfterOrderByDateAsc(LocalDateTime.now())
                .stream()
                .map(this::mapToEventResponseDto)
                .collect(Collectors.toList());
    }

}