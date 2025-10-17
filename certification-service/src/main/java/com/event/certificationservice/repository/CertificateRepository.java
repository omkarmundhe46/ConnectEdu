package com.event.certificationservice.repository;

import com.event.certificationservice.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    boolean existsByEventIdAndUserId(Long eventId, Long userId);
    // ADD THIS METHOD: to find the certificate record for the download endpoint
    Optional<Certificate> findByEventIdAndUserId(Long eventId, Long userId);
}