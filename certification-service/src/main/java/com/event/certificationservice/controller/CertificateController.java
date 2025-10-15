package com.event.certificationservice.controller;

import com.event.certificationservice.entity.Certificate;
import com.event.certificationservice.dto.CertificateResponseDto;
import com.event.certificationservice.service.CertificateService;
import lombok.RequiredArgsConstructor;
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
}