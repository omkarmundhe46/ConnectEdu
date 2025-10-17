package com.event.certificationservice.controller;

import com.event.certificationservice.entity.Certificate;
import com.event.certificationservice.dto.CertificateResponseDto;
import com.event.certificationservice.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    @PostMapping("/event/{eventId}/user/{userId}")
    public ResponseEntity<CertificateResponseDto> generateAndSend(@PathVariable Long eventId, @PathVariable Long userId) {
        Certificate saved = certificateService.generateSaveAndSend(eventId, userId);

        CertificateResponseDto dto = new CertificateResponseDto(
                saved.getId(),
                saved.getUserId(),
                saved.getEventId(),
                saved.getFilePath(),
                saved.getIssuedAt()
        );

        return ResponseEntity.ok(dto);
    }

    /**
     * NEW DOWNLOAD ENDPOINT (for Participants):
     * Allows a user to download their certificate directly after 7 PM on the event day.
     */
    @GetMapping("/event/{eventId}/user/{userId}/download")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable Long eventId, @PathVariable Long userId) {
        try {
            byte[] pdfBytes = certificateService.getCertificateForDownload(eventId, userId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Certificate-Event-" + eventId + ".pdf");

            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (IllegalStateException e) {
            // This handles the case where the certificate is not yet available (before 7 PM)
            return ResponseEntity.status(425).body(e.getMessage().getBytes()); // 425 Too Early
        }
    }
}