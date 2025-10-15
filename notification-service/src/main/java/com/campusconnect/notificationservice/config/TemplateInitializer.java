package com.campusconnect.notificationservice.config;

import com.campusconnect.notificationservice.entity.EmailTemplate;
import com.campusconnect.notificationservice.repository.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TemplateInitializer implements CommandLineRunner {
    
    private final EmailTemplateRepository templateRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeTemplates();
    }

    private void initializeTemplates() {
        createTemplateIfNotExists("WELCOME_USER",
                "Welcome to CampusConnect, [(${name})]!",
                "Hi [(${name})], your account has been successfully created. Welcome to CampusConnect!");

        createTemplateIfNotExists("EVENT_CREATED",
                "New Event: [(${eventTitle})]",
                "Dear [(${name})], a new event '[(${eventTitle})]' has been created by [(${clubName})]. It starts on [(${eventDate})] at [(${location})].");

        createTemplateIfNotExists("WELCOME_CLUB_MEMBER",
                "You joined [(${clubName})] as [(${role})]",
                "<p>Hello,</p><p>You are now a <strong>[(${role})]</strong> of the <strong>[(${clubName})]</strong>.</p><p>Welcome to the club!</p><p>Best regards,<br>CampusConnect Team</p>");

        createTemplateIfNotExists("EVENT_REGISTRATION_CONFIRM",
                "Registration Confirmed for [(${eventName})]",
                "<p>Dear Participant,</p><p>You are registered for <strong>[(${eventName})]</strong> on <strong>[(${eventDate})]</strong>.</p><p>Hosted by <strong>[(${clubName})]</strong>.</p><p>We look forward to seeing you there!</p><p>Best regards,<br>CampusConnect Team</p>");
    }

    private void createTemplateIfNotExists(String code, String subject, String body) {
        if (!templateRepository.existsByCode(code)) {
            EmailTemplate template = new EmailTemplate();
            template.setCode(code);
            template.setSubjectTemplate(subject);
            template.setBodyTemplate(body);
            templateRepository.save(template);
            log.info("Created email template: {}", code);
        }
    }
}