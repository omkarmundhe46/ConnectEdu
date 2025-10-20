package com.campusconnect.eventservice.service;

import com.campusconnect.eventservice.client.UserClient;
import com.campusconnect.eventservice.dto.ParticipantResponseDto;
import com.campusconnect.eventservice.dto.UserDto;
import com.campusconnect.eventservice.entity.Event;
import com.campusconnect.eventservice.entity.EventParticipant;
import com.campusconnect.eventservice.repository.EventParticipantRepository;
import com.campusconnect.eventservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final EventRepository eventRepository;
    private final EventParticipantRepository eventParticipantRepository;

    public List<ParticipantResponseDto> getParticipantsByClubAndEvent(Long clubId, Long eventId) {
        // 1️⃣ Verify the event exists and belongs to the club
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getClubId().equals(clubId)) {
            throw new RuntimeException("Event does not belong to this club");
        }

        // 2️⃣ Fetch participants for the event
        List<EventParticipant> participants = eventParticipantRepository.findByEventId(eventId);

        // 3️⃣ Map to DTO (no user details here)
        // --- UPDATED MAPPING ---
        return participants.stream().map(participant -> {
            ParticipantResponseDto dto = new ParticipantResponseDto();
            dto.setId(participant.getId());
            dto.setEventId(participant.getEventId());
            dto.setUserId(participant.getUserId());
            dto.setRegisteredAt(participant.getRegisteredAt());
            // Map the new fields
            dto.setCollege(participant.getCollege());
            dto.setMobileNumber(participant.getMobileNumber());
            dto.setAddress(participant.getAddress());
            dto.setPaymentId(participant.getPaymentId());
            return dto;
        }).collect(Collectors.toList());
    }
}
