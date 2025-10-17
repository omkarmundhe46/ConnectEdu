package com.event.certificationservice.service;

//import com.event.certificationservice.client.NotificationClient;
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final UserClient userClient;
    private final EventClient eventClient;
//    private final NotificationClient notificationClient;
    private final CertificateKafkaProducer certificateKafkaProducer;

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

//    /**
//     * Generate, save metadata and send PDF via notification service.
//     */
//
//    public Certificate generateSaveAndSend(Long eventId, Long userId) {
//        try {
//            // Generate PDF bytes
//            byte[] pdf = generateCertificatePdf(eventId, userId);
//
//            // Save to disk
//            Path uploadsDir = Paths.get("uploads", "certificates");
//            Files.createDirectories(uploadsDir);
//            String filename = "event_" + eventId + "user" + userId + ".pdf";
//            Path filePath = uploadsDir.resolve(filename);
//            Files.write(filePath, pdf, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
//
//            // Save metadata to DB
//            Certificate certificate = new Certificate();
//            certificate.setEventId(eventId);
//            certificate.setUserId(userId);
//            certificate.setFilePath(filePath.toString());
//            certificate.setIssuedAt(LocalDateTime.now());
//            Certificate saved = certificateRepository.save(certificate);
//
//            // Send notification (PDF bytes included)
//            UserResponseDto user = userClient.getUserById(userId);
//            EventResponseDto event = eventClient.getEventById(eventId);
//
//            CertificateNotificationRequest notificationRequest = new CertificateNotificationRequest(
//                    userId,
//                    user.getName(),
//                    user.getEmail(),
//                    event.getName(),
//                    event.getDate().toString(),
//                    pdf
//            );
//
//            try {
//                notificationClient.sendCertificate(notificationRequest);
//                log.info("Notification-service called for certificate for user {}", userId);
//            } catch (FeignException fe) {
//                log.error("Failed to call notification-service: {}", fe.contentUTF8(), fe);
//                // optionally you might want to set a status column in DB for "notification_failed"
//            }
//
//            return saved;
//
//        } catch (Exception e) {
//            log.error("Error in generateSaveAndSend", e);
//            throw new RuntimeException(e);
//        }
//    }


//    /**
//     * Generate, save metadata and send a message to Kafka for notification.
//     */
//    public Certificate generateSaveAndSend(Long eventId, Long userId) {
//        try {
//            // Generate PDF bytes
//            byte[] pdf = generateCertificatePdf(eventId, userId);
//
//            // Save to disk
//            Path uploadsDir = Paths.get("uploads", "certificates");
//            Files.createDirectories(uploadsDir);
//            String filename = "event_" + eventId + "_user_" + userId + ".pdf";
//            Path filePath = uploadsDir.resolve(filename);
//            Files.write(filePath, pdf, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
//
//            // Save metadata to DB
//            Certificate certificate = new Certificate();
//            certificate.setEventId(eventId);
//            certificate.setUserId(userId);
//            certificate.setFilePath(filePath.toString());
//            certificate.setIssuedAt(LocalDateTime.now());
//            Certificate saved = certificateRepository.save(certificate);
//
//            // Fetch user and event details for the notification message
//            UserResponseDto user = userClient.getUserById(userId);
//            EventResponseDto event = eventClient.getEventById(eventId);
//
//            CertificateNotificationRequest notificationRequest = new CertificateNotificationRequest(
//                    userId,
//                    user.getName(),
//                    user.getEmail(),
//                    event.getName(),
//                    event.getDate().toString(),
//                    pdf
//            );
//
//            // **MODIFIED PART**: Send notification via Kafka instead of Feign
//            certificateKafkaProducer.sendCertificateNotification(notificationRequest);
//            log.info("Certificate notification message for user {} has been sent to the Kafka queue.", userId);
//
//            return saved;
//
//        } catch (Exception e) {
//            log.error("Error in generateSaveAndSend", e);
//            throw new RuntimeException(e);
//        }
//    }
//    public void generateCertificateForParticipant(Long eventId, Long userId) {
//        // Check if a certificate already exists for this user and event
//        if (certificateRepository.existsByEventIdAndUserId(eventId, userId)) {
//            log.warn("Certificate for user {} and event {} already exists. Skipping.", userId, eventId);
//            return; // Do nothing if it's already been issued
//        }
//
//        // If it doesn't exist, call the original method to create and send it
//        log.info("No existing certificate found. Generating new certificate for user {} and event {}.", userId, eventId);
//        generateSaveAndSend(eventId, userId);
//    }


    /**
     * MANUAL FLOW: This is your original method. It now uses a helper to avoid
     * re-generating the PDF, but its primary function is to send the Kafka notification.
     */
    public Certificate generateSaveAndSend(Long eventId, Long userId) {
        // Step 1: Ensure a certificate file exists by finding or creating it.
        Certificate certificate = findOrCreateCertificate(eventId, userId);

        // Step 2: Read the PDF from disk and send the Kafka message for email notification.
        try {
            UserResponseDto user = userClient.getUserById(userId);
            EventResponseDto event = eventClient.getEventById(eventId);
            byte[] pdfBytes = Files.readAllBytes(Paths.get(certificate.getFilePath()));

            CertificateNotificationRequest notificationRequest = new CertificateNotificationRequest(
                    userId, user.getName(), user.getEmail(), event.getName(), event.getDate().toString(), pdfBytes
            );

            certificateKafkaProducer.sendCertificateNotification(notificationRequest);
            log.info("Certificate email notification has been queued for user {}.", userId);

            return certificate;
        } catch (IOException e) {
            log.error("Failed to read certificate file for notification: {}", e.getMessage());
            throw new RuntimeException("Failed to read certificate for notification.", e);
        }
    }




    /**
     * NEW METHOD FOR DOWNLOAD FLOW:
     * Gets the certificate PDF bytes for direct download. It enforces the 7 PM rule.
     */
    public byte[] getCertificateForDownload(Long eventId, Long userId) {
        // Step 1: Check the business rule (is it after 7 PM on the event day?)
        EventResponseDto event = eventClient.getEventById(eventId);
        LocalDate eventDate = event.getDate();
        LocalDateTime activationTime = LocalDateTime.of(eventDate, LocalTime.of(19, 0)); // 7 PM

        if (LocalDateTime.now().isBefore(activationTime)) {
            throw new IllegalStateException("Certificate is not yet available for download. Please check back after 7 PM on the event date.");
        }

        // Step 2: Find or create the certificate file
        Certificate certificate = findOrCreateCertificate(eventId, userId);

        // Step 3: Read the PDF file from disk and return its bytes
        try {
            return Files.readAllBytes(Paths.get(certificate.getFilePath()));
        } catch (IOException e) {
            log.error("Could not read certificate file from path {}: {}", certificate.getFilePath(), e.getMessage());
            throw new RuntimeException("Error retrieving certificate file.", e);
        }
    }

    /**
     * NEW PRIVATE HELPER METHOD:
     * Checks if a certificate exists in the DB. If yes, it returns it.
     * If not, it generates the PDF, saves it, and returns the new DB record.
     */
    private Certificate findOrCreateCertificate(Long eventId, Long userId) {
        Optional<Certificate> existingCert = certificateRepository.findByEventIdAndUserId(eventId, userId);
        if (existingCert.isPresent()) {
            log.info("Found existing certificate for user {} and event {}.", userId, eventId);
            return existingCert.get();
        }

        log.info("No existing certificate found. Generating new one for user {} and event {}.", userId, eventId);
        try {
            byte[] pdf = generateCertificatePdf(eventId, userId);
            Path uploadsDir = Paths.get("uploads", "certificates");
            Files.createDirectories(uploadsDir);
            String filename = "event_" + eventId + "_user_" + userId + ".pdf";
            Path filePath = uploadsDir.resolve(filename);
            Files.write(filePath, pdf, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            Certificate newCertificate = new Certificate();
            newCertificate.setEventId(eventId);
            newCertificate.setUserId(userId);
            newCertificate.setFilePath(filePath.toString());
            newCertificate.setIssuedAt(LocalDateTime.now());

            return certificateRepository.save(newCertificate);

        } catch (Exception e) {
            log.error("Error in findOrCreateCertificate for user {} and event {}: {}", userId, eventId, e.getMessage());
            throw new RuntimeException("Failed to generate and save certificate.", e);
        }
    }
}