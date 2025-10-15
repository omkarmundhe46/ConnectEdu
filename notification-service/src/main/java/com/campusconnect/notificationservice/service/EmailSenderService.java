 package com.campusconnect.notificationservice.service;

import com.campusconnect.notificationservice.entity.NotificationLog;
import com.campusconnect.notificationservice.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSenderService {
    
    private final JavaMailSender mailSender;
    private final NotificationLogRepository logRepository;
    
    @Value("${notify.default-from}")
    private String defaultFrom;

    @Async
    public CompletableFuture<Void> sendEmailAsync(NotificationLog notificationLog) {
        log.info("Sending async email to {}", notificationLog.getToEmail());

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(defaultFrom);
            helper.setTo(notificationLog.getToEmail());
            helper.setSubject(notificationLog.getSubject());
            helper.setText(notificationLog.getBody(), true);
            
            mailSender.send(message);

            logRepository.save(notificationLog);
            
            log.info("Email sent successfully to: {}", notificationLog.getToEmail());
            
        } catch (Exception e) {
            log.error("Failed to send email to: {}", notificationLog.getToEmail(), e);

            notificationLog.setErrorMessage(e.getMessage());
            logRepository.save(notificationLog);
        }
        
        return CompletableFuture.completedFuture(null);
    }
}