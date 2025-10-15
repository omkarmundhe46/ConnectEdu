package com.event.certificationservice.repository;

import com.event.certificationservice.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    boolean existsByEventIdAndUserId(Long eventId, Long userId);
}