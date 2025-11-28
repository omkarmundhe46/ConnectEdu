package com.event.certificationservice.controller;

import com.event.certificationservice.dto.CertificateConfigDto;
import com.event.certificationservice.entity.EventCertificateConfig;
import com.event.certificationservice.enums.CertificateTemplateType;
import com.event.certificationservice.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;


@RestController
@RequestMapping("/api/certificates/config")
@RequiredArgsConstructor
public class CertificateConfigController {

    private final CertificateService certificateService;

    @GetMapping("/templates")
    public ResponseEntity<List<CertificateTemplateType>> getTemplates() {
        return ResponseEntity.ok(Arrays.asList(CertificateTemplateType.values()));
    }

    // 3. Get configuration for an event (to pre-fill the form)
    @GetMapping("/event/{eventId}")
    public ResponseEntity<EventCertificateConfig> getConfig(@PathVariable Long eventId) {
        return ResponseEntity.ok(certificateService.getConfig(eventId));
    }

    @PostMapping("/event/{eventId}")
    public ResponseEntity<Void> saveConfig(@PathVariable Long eventId, @RequestParam("template") CertificateTemplateType template) {
        certificateService.saveConfig(eventId, template);
        return ResponseEntity.ok().build();
    }

}