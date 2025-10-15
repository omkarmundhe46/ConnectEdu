//package com.campusconnect.eventservice.service;
//
//import org.springframework.stereotype.Service;
//
//import com.campusconnect.eventservice.client.ClubClient;
//import com.campusconnect.eventservice.client.NotificationClient;
//import com.campusconnect.eventservice.client.UserClient;
//import com.campusconnect.eventservice.dto.UserDto;
//import com.campusconnect.eventservice.entity.Event;
//import com.campusconnect.eventservice.repository.EventParticipantRepository;
//import com.campusconnect.eventservice.repository.EventRepository;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import net.sf.jasperreports.engine.JRException;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class CirtificateService {
//	
//	public byte[] generateCertificate(Long eventId, Long userId) throws JRException {
//        try {
//            Event event = eventgetEventById(eventId);
//            UserDto user = userRepository.findById(userId).orElse(null);
//
//            if (event == null) {
//                throw new IllegalArgumentException("Event not found with id: " + eventId);
//            }
//            if (user == null) {
//                throw new IllegalArgumentException("User not found with id: " + userId);
//            }
//            if (!event.getParticipants().contains(user)) {
//                throw new IllegalArgumentException("User is not a participant of this event");
//            }
//
//            return certificateService.generateCertificate(event, user);
//
//        } catch (IllegalArgumentException e) {
//            throw new JRException("Certificate generation failed: " + e.getMessage());
//        }
//    }
//}
//
//}
