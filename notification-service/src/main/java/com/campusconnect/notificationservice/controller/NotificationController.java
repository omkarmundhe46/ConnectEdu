

package com.campusconnect.notificationservice.controller;

import com.campusconnect.notificationservice.dto.CertificateNotificationRequest;
import com.campusconnect.notificationservice.dto.ClubMemberAddedRequest;
import com.campusconnect.notificationservice.dto.EventParticipationDTO;
import com.campusconnect.notificationservice.dto.EventResponseDto;
import com.campusconnect.notificationservice.dto.NotificationResponse;
import com.campusconnect.notificationservice.dto.UserRegisteredRequest;
import com.campusconnect.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

	private final NotificationService notificationService;
	

	@PostMapping("/user-registered")
	public ResponseEntity<NotificationResponse> notifyUserRegistered(@RequestBody UserRegisteredRequest request) {
		log.info("📩 Received user registered notification for userId={}", request.getUserId());
		return ResponseEntity.ok(notificationService.notifyUserRegistered(request));
	}

	@PostMapping("/event-created")
	public ResponseEntity<NotificationResponse> notifyEventCreated(@Valid @RequestBody EventResponseDto request) {
		log.info("📩 Received event created notification for eventId={}, clubId={}", request.getId(),
				request.getClubId());
		NotificationResponse response = notificationService.notifyEventCreated(request);
		return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
	}

	@PostMapping(value = "/event-participation", consumes = "application/json")
	public ResponseEntity<NotificationResponse> handleEventParticipation(@RequestBody EventParticipationDTO request) {
		log.info("📩 Received participation notification request: userId={}, eventId={}", request.getUserId(),
				request.getEventId());
		NotificationResponse response = notificationService.notifyEventParticipation(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/club-member-added")
	public ResponseEntity<NotificationResponse> notifyClubMemberAdded(@RequestBody ClubMemberAddedRequest request) {
		log.info("📩 Received club member added notification: userId={}, clubId={}", request.getUserId(),
				request.getClubId());
		NotificationResponse response = notificationService.notifyClubMemberAdded(request);
		return ResponseEntity.ok(response);
	}
//	@PostMapping("/certificate-issued")
//    public ResponseEntity<String> sendCertificate(@RequestBody CertificateNotificationRequest request) {
//		notificationService.sendCertificateEmail(request);
//        return ResponseEntity.ok("Certificate email queued/sent to " + request.getUserEmail());
//    }
}
