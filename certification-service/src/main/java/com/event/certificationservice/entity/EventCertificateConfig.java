package com.event.certificationservice.entity;

import com.event.certificationservice.enums.CertificateTemplateType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "event_certificate_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventCertificateConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CertificateTemplateType templateType;
}