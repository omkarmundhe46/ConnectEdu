package com.event.certificationservice.repository;

import com.event.certificationservice.entity.EventCertificateConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EventCertificateConfigRepository extends JpaRepository<EventCertificateConfig, Long> {
    Optional<EventCertificateConfig> findByEventId(Long eventId);
}