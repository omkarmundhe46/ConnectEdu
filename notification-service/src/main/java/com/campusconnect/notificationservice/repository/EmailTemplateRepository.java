package com.campusconnect.notificationservice.repository;

import com.campusconnect.notificationservice.entity.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
    Optional<EmailTemplate> findByCode(String code);
    boolean existsByCode(String code);
}