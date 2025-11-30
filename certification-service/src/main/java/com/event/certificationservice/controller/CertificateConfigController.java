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
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/certificates/config")
@RequiredArgsConstructor
public class CertificateConfigController {

    private final CertificateService certificateService;

    @GetMapping("/templates")
    public ResponseEntity<List<CertificateTemplateType>> getTemplates(@RequestParam(required = false) String category) {

        // If no category is sent, return everything (or just generics)
        if (category == null || category.isEmpty()) {
            return ResponseEntity.ok(Arrays.asList(CertificateTemplateType.values()));
        }

        // Filter the Enum values based on the category
        List<CertificateTemplateType> filtered = Arrays.stream(CertificateTemplateType.values())
                .filter(t -> t.getCategory().equalsIgnoreCase(category) || t.getCategory().equalsIgnoreCase("ALL"))
                .collect(Collectors.toList());

        return ResponseEntity.ok(filtered);
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