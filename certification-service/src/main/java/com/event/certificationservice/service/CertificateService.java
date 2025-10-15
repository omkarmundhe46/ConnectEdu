package com.event.certificationservice.service;

import com.event.certificationservice.client.NotificationClient;
import com.event.certificationservice.dto.CertificateNotificationRequest;
import com.event.certificationservice.dto.EventResponseDto;
import com.event.certificationservice.dto.UserResponseDto;
import com.event.certificationservice.entity.Certificate;
import com.event.certificationservice.repository.CertificateRepository;
import com.event.certificationservice.client.UserClient;
import com.event.certificationservice.client.EventClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final UserClient userClient;
    private final EventClient eventClient;
    private final NotificationClient notificationClient;

    /**
     * Generate PDF bytes using JasperReports.
     */
    public byte[] generateCertificatePdf(Long eventId, Long userId) throws JRException {
        try {
            UserResponseDto user = userClient.getUserById(userId);
            EventResponseDto event = eventClient.getEventById(eventId);

            ClassPathResource jrxmlRes = new ClassPathResource("certi/certificate.jrxml");
            try (InputStream jrxmlStream = jrxmlRes.getInputStream()) {
                JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);

                Map<String, Object> params = new HashMap<>();
                params.put("userName", user.getName());
                params.put("eventName", event.getName());
                params.put("eventDate", event.getDate().toString());

                // Images from resources (must exist under src/main/resources/certi/images/)
                params.put("backgroundImage", getResourceStream("certi/images/x.png"));
                params.put("logoImage", getResourceStream("certi/images/clg_logo_White.png"));
                params.put("aImage", getResourceStream("certi/images/A.png"));
                params.put("signatureImage1", getResourceStream("certi/images/sign1.png"));
                params.put("signatureImage2", getResourceStream("certi/images/sign2.png"));
                params.put("signatureImage3", getResourceStream("certi/images/sign3.png"));
                params.put("signatureImage4", getResourceStream("certi/images/sign4.png"));

                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
                byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
                return pdfBytes;
            }
        } catch (FeignException.NotFound nf) {
            log.error("Dependent resource not found: {}", nf.getMessage());
            throw new RuntimeException("User or Event not found", nf);
        } catch (Exception e) {
            log.error("Certificate generation failed", e);
            throw new RuntimeException("Certificate generation failed: " + e.getMessage(), e);
        }
    }

    private InputStream getResourceStream(String classpathPath) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(classpathPath);
        if (is == null) {
            throw new IllegalArgumentException("Resource not found in classpath: " + classpathPath);
        }
        return is;
    }

    /**
     * Generate, save metadata and send PDF via notification service.
     */
    public Certificate generateSaveAndSend(Long eventId, Long userId) {
        try {
            // Generate PDF bytes
            byte[] pdf = generateCertificatePdf(eventId, userId);

            // Save to disk
            Path uploadsDir = Paths.get("uploads", "certificates");
            Files.createDirectories(uploadsDir);
            String filename = "event_" + eventId + "user" + userId + ".pdf";
            Path filePath = uploadsDir.resolve(filename);
            Files.write(filePath, pdf, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // Save metadata to DB
            Certificate certificate = new Certificate();
            certificate.setEventId(eventId);
            certificate.setUserId(userId);
            certificate.setFilePath(filePath.toString());
            certificate.setIssuedAt(LocalDateTime.now());
            Certificate saved = certificateRepository.save(certificate);

            // Send notification (PDF bytes included)
            UserResponseDto user = userClient.getUserById(userId);
            EventResponseDto event = eventClient.getEventById(eventId);

            CertificateNotificationRequest notificationRequest = new CertificateNotificationRequest(
                    userId,
                    user.getName(),
                    user.getEmail(),
                    event.getName(),
                    event.getDate().toString(),
                    pdf
            );

            try {
                notificationClient.sendCertificate(notificationRequest);
                log.info("Notification-service called for certificate for user {}", userId);
            } catch (FeignException fe) {
                log.error("Failed to call notification-service: {}", fe.contentUTF8(), fe);
                // optionally you might want to set a status column in DB for "notification_failed"
            }

            return saved;

        } catch (Exception e) {
            log.error("Error in generateSaveAndSend", e);
            throw new RuntimeException(e);
        }
    }
}