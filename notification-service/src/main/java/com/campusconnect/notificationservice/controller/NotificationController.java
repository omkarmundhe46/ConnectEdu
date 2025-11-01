package com.campusconnect.notificationservice.controller;
import com.campusconnect.notificationservice.dto.CertificateNotificationRequest;
import com.campusconnect.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

	private final NotificationService notificationService;

	@PostMapping("/certificate-issued")
    public ResponseEntity<String> sendCertificate(@RequestBody CertificateNotificationRequest request) {
		notificationService.sendCertificateEmail(request);
        return ResponseEntity.ok("Certificate email queued/sent to " + request.getUserEmail());
    }
}
