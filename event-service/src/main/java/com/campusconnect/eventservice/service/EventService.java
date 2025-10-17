package com.campusconnect.eventservice.service;

import com.campusconnect.eventservice.dto.EventParticipationDTO;
import com.campusconnect.eventservice.dto.EventRequestDto;
import com.campusconnect.eventservice.dto.EventResponseDto;
import com.campusconnect.eventservice.dto.ParticipantResponseDto;
import com.campusconnect.eventservice.entity.Event;
import com.campusconnect.eventservice.entity.EventParticipant;
import com.campusconnect.eventservice.exception.*;
import com.campusconnect.eventservice.kafka.EventKafkaProducer;
import com.campusconnect.eventservice.repository.EventRepository;
import com.campusconnect.eventservice.repository.EventParticipantRepository;
import com.campusconnect.eventservice.client.CertificateClient;
import com.campusconnect.eventservice.client.ClubClient;
import com.campusconnect.eventservice.client.NotificationClient;
import com.campusconnect.eventservice.client.UserClient;
import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {
    
    private final EventRepository eventRepository;
    private final EventParticipantRepository participantRepository;
//    private final NotificationClient notificationClient;
    private final ClubClient clubClient;
    private final EventKafkaProducer eventKafkaProducer; // ADD THIS
    private final UserClient userClient;
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
//        Event event = eventRepository.findByIdAndClubId(eventId, clubId)
//                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId + " for club: " + clubId));
//       log.info("Saving PArticipate in Repo");
//
//        // Validate user exists
//        try {
//            userClient.getUserById(participantRequestDto.getUserId());
//        } catch (FeignException.NotFound e) {
//            throw new UserNotFoundException("User not found with id: " + participantRequestDto.getUserId());
//        }
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
//        notificationClient.notifyEventParticipation(participantRequestDto);
//        log.info("Event participation notification sent for user: {} in event: {}", participantRequestDto.getUserId(), eventId);
//        return mapToParticipantResponseDto(savedParticipant);
//    }


    public ParticipantResponseDto addParticipantToEvent(Long clubId, Long eventId, EventParticipationDTO participantRequestDto) {
        Event event = eventRepository.findByIdAndClubId(eventId, clubId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId + " for club: " + clubId));

        try {
            userClient.getUserById(participantRequestDto.getUserId());
        } catch (FeignException.NotFound e) {
            throw new UserNotFoundException("User not found with id: " + participantRequestDto.getUserId());
        }

        if (participantRepository.existsByEventIdAndUserId(eventId, participantRequestDto.getUserId())) {
            throw new DuplicateParticipationException("User already participating in this event");
        }

        EventParticipant participant = new EventParticipant();
        participant.setEventId(eventId);
        participant.setUserId(participantRequestDto.getUserId());

        EventParticipant savedParticipant = participantRepository.save(participant);
        log.info("Participant saved: {}", savedParticipant);

        // --- THIS IS THE FIX ---
        // Before sending to Kafka, ensure the DTO has the correct eventId from the URL path.
        participantRequestDto.setEventId(eventId);

        // **MODIFIED PART**: Send notification via Kafka
        eventKafkaProducer.sendEventParticipationNotification(participantRequestDto);
        log.info("Event participation notification queued for user: {} in event: {}", participantRequestDto.getUserId(), eventId);

        return mapToParticipantResponseDto(savedParticipant);
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
}