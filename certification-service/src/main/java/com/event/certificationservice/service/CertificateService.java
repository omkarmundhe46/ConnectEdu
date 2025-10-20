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
    private final S3StorageService s3StorageService; // Inject the new service

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


    // manual flow of generating, saving and sending certificate : uses uploads folder to save files
    /**
     * MANUAL FLOW: This is your original method. It now uses a helper to avoid
     * re-generating the PDF, but its primary function is to send the Kafka notification.
     */
//    public Certificate generateSaveAndSend(Long eventId, Long userId) {
//        // Step 1: Ensure a certificate file exists by finding or creating it.
//        Certificate certificate = findOrCreateCertificate(eventId, userId);
//
//        // Step 2: Read the PDF from disk and send the Kafka message for email notification.
//        try {
//            UserResponseDto user = userClient.getUserById(userId);
//            EventResponseDto event = eventClient.getEventById(eventId);
//            byte[] pdfBytes = Files.readAllBytes(Paths.get(certificate.getFilePath()));
//
//            CertificateNotificationRequest notificationRequest = new CertificateNotificationRequest(
//                    userId, user.getName(), user.getEmail(), event.getName(), event.getDate().toString(), pdfBytes
//            );
//
//            certificateKafkaProducer.sendCertificateNotification(notificationRequest);
//            log.info("Certificate email notification has been queued for user {}.", userId);
//
//            return certificate;
//        } catch (IOException e) {
//            log.error("Failed to read certificate file for notification: {}", e.getMessage());
//            throw new RuntimeException("Failed to read certificate for notification.", e);
//        }
//    }



    /**
     * MANUAL FLOW: Generates a certificate, uploads it to S3, and sends a notification.
     */
//    public Certificate generateSaveAndSend(Long eventId, Long userId) {
//        // Step 1: Ensure a certificate exists and is stored in S3.
//        Certificate certificate = findOrCreateCertificate(eventId, userId);
//
//        // Step 2: Download the PDF from S3 to get its bytes for the Kafka message.
//        byte[] pdfBytes = s3StorageService.downloadFile(certificate.getFilePath());
//
//        UserResponseDto user = userClient.getUserById(userId);
//        EventResponseDto event = eventClient.getEventById(eventId);
//
//        CertificateNotificationRequest notificationRequest = new CertificateNotificationRequest(
//                userId, user.getName(), user.getEmail(), event.getName(), event.getDate().toString(), pdfBytes
//        );
//
//        certificateKafkaProducer.sendCertificateNotification(notificationRequest);
//        log.info("Certificate email notification has been queued for user {}.", userId);
//
//        return certificate;
//    }


    public Certificate generateSaveAndSend(Long eventId, Long userId) {
        // --- REFACTORED LOGIC ---
        // 1. Generate the PDF bytes first.
        byte[] pdfBytes;
        try {
            pdfBytes = generateCertificatePdf(eventId, userId);
        } catch (JRException e) {
            log.error("Failed to generate PDF bytes for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to generate PDF.", e);
        }

        // 2. Ensure the certificate is created/updated and uploaded to S3.
        Certificate certificate = findOrCreateAndUploadCertificate(eventId, userId, pdfBytes);

        // 3. Send the notification using the bytes we already have. No need to re-download.
        UserResponseDto user = userClient.getUserById(userId);
        EventResponseDto event = eventClient.getEventById(eventId);
        CertificateNotificationRequest notificationRequest = new CertificateNotificationRequest(
                userId, user.getName(), user.getEmail(), event.getName(), event.getDate().toString(), pdfBytes
        );
        certificateKafkaProducer.sendCertificateNotification(notificationRequest);
        log.info("Certificate email notification has been queued for user {}.", userId);

        return certificate;
    }



    // manual method : for download certificate :  in upload folder
    /**
     * NEW METHOD FOR DOWNLOAD FLOW:
     * Gets the certificate PDF bytes for direct download. It enforces the 7 PM rule.
     */
//    public byte[] getCertificateForDownload(Long eventId, Long userId) {
//        // Step 1: Check the business rule (is it after 7 PM on the event day?)
//        EventResponseDto event = eventClient.getEventById(eventId);
//        LocalDate eventDate = event.getDate();
//        LocalDateTime activationTime = LocalDateTime.of(eventDate, LocalTime.of(19, 0)); // 7 PM
//
//        if (LocalDateTime.now().isBefore(activationTime)) {
//            throw new IllegalStateException("Certificate is not yet available for download. Please check back after 7 PM on the event date.");
//        }
//
//        // Step 2: Find or create the certificate file
//        Certificate certificate = findOrCreateCertificate(eventId, userId);
//
//        // Step 3: Read the PDF file from disk and return its bytes
//        try {
//            return Files.readAllBytes(Paths.get(certificate.getFilePath()));
//        } catch (IOException e) {
//            log.error("Could not read certificate file from path {}: {}", certificate.getFilePath(), e.getMessage());
//            throw new RuntimeException("Error retrieving certificate file.", e);
//        }
//    }


    /**
     * DOWNLOAD FLOW: Gets the certificate from S3 for direct download.
     */
    public byte[] getCertificateForDownload(Long eventId, Long userId) {
        // Step 1: Check the business rule (is it after 7 PM on the event day?)
        EventResponseDto event = eventClient.getEventById(eventId);
        LocalDate eventDate = event.getDate();
        LocalDateTime activationTime = LocalDateTime.of(eventDate, LocalTime.of(19, 0));

        if (LocalDateTime.now().isBefore(activationTime)) {
            throw new IllegalStateException("Certificate is not yet available for download. Please check back after 7 PM on the event date.");
        }

        // --- REFACTORED LOGIC ---
        // 1. Ensure the certificate exists and is on S3 by calling the helper.
        Certificate certificate = findOrCreateAndUploadCertificate(eventId, userId, null);

        // 2. Download the now-guaranteed-to-exist file from S3.
        return s3StorageService.downloadFile(certificate.getFilePath());
    }



    /**
     * NEW PRIVATE HELPER METHOD:
     * Checks if a certificate exists in the DB. If yes, it returns it.
     * If not, it generates the PDF, saves it, and returns the new DB record.
     */
//    private Certificate findOrCreateCertificate(Long eventId, Long userId) {
//        Optional<Certificate> existingCert = certificateRepository.findByEventIdAndUserId(eventId, userId);
//        if (existingCert.isPresent()) {
//            log.info("Found existing certificate for user {} and event {}.", userId, eventId);
//            return existingCert.get();
//        }
//
//        log.info("No existing certificate found. Generating new one for user {} and event {}.", userId, eventId);
//        try {
//            byte[] pdf = generateCertificatePdf(eventId, userId);
//
//            // --- THIS IS THE CHANGE ---
//            // Upload the generated PDF bytes to S3
//            String fileUrl = s3StorageService.uploadPdf(pdfBytes, "event_" + eventId + "_user_" + userId + ".pdf");
//
//            Certificate certificate = new Certificate();
//            certificate.setEventId(eventId);
//            certificate.setUserId(userId);
//            certificate.setFilePath(fileUrl); // Save the S3 URL
//            certificate.setIssuedAt(LocalDateTime.now());
//
//            return certificateRepository.save(certificate);
//
//        } catch (Exception e) {
//            log.error("Error in findOrCreateCertificate: {}", e.getMessage());
//            throw new RuntimeException(e);
//        }
//    }

    /**
     * Helper method to find a certificate in the DB or create it and upload to S3.
     */
    private Certificate findOrCreateCertificate(Long eventId, Long userId) {
        Optional<Certificate> existingCert = certificateRepository.findByEventIdAndUserId(eventId, userId);
        if (existingCert.isPresent()) {
            log.info("Found existing certificate for user {} and event {}.", userId, eventId);
            return existingCert.get();
        }

        log.info("No existing certificate found. Generating and uploading new one for user {} and event {}.", userId, eventId);
        try {
            byte[] pdfBytes = generateCertificatePdf(eventId, userId);
            String fileName = "event_" + eventId + "_user_" + userId + ".pdf";

            // Upload the generated PDF bytes to S3
            String fileUrl = s3StorageService.uploadPdf(pdfBytes, fileName);

            Certificate certificate = new Certificate();
            certificate.setEventId(eventId);
            certificate.setUserId(userId);
            certificate.setFilePath(fileUrl); // Save the S3 URL
            certificate.setIssuedAt(LocalDateTime.now());

            return certificateRepository.save(certificate);

        } catch (Exception e) {
            log.error("Error in findOrCreateCertificate: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // ADD THIS NEW METHOD for the scheduler
    public void generateAndNotifyParticipant(Long eventId, Long userId) {
        // This safety check prevents sending the same certificate email twice
        if (certificateRepository.existsByEventIdAndUserId(eventId, userId)) {
            log.warn("Certificate for user {} and event {} already exists. Skipping email notification.", userId, eventId);
            return;
        }

        // If it doesn't exist, call the original method to create, upload, and send the notification.
        log.info("Generating and sending new certificate for user {} and event {}.", userId, eventId);
        generateSaveAndSend(eventId, userId);
    }


    /**
     * Helper method that finds a certificate. If the DB record points to an old local file
     * or doesn't exist, it generates/regenerates the PDF, uploads it to S3, and saves the record.
     */
    private Certificate findOrCreateAndUploadCertificate(Long eventId, Long userId, byte[] preGeneratedPdfBytes) {
        Optional<Certificate> existingCertOpt = certificateRepository.findByEventIdAndUserId(eventId, userId);

        // If a record exists but has an old local path, we must treat it as non-existent and re-upload.
        if (existingCertOpt.isPresent() && existingCertOpt.get().getFilePath().startsWith("https://")) {
            log.info("Found existing, valid S3 certificate for user {} and event {}.", userId, eventId);
            return existingCertOpt.get();
        }

        log.info("No valid S3 certificate found. Generating/uploading new one for user {} and event {}.", userId, eventId);
        try {
            // Use pre-generated bytes if available (from generateSaveAndSend), otherwise generate new ones.
            byte[] pdfBytes = (preGeneratedPdfBytes != null) ? preGeneratedPdfBytes : generateCertificatePdf(eventId, userId);

            String fileName = "event_" + eventId + "_user_" + userId + ".pdf";
            String fileUrl = s3StorageService.uploadPdf(pdfBytes, fileName);

            // Use existing record if it's stale, otherwise create a new one.
            Certificate certificate = existingCertOpt.orElse(new Certificate());
            certificate.setEventId(eventId);
            certificate.setUserId(userId);
            certificate.setFilePath(fileUrl); // Save/Update to the S3 URL
            certificate.setIssuedAt(LocalDateTime.now());

            return certificateRepository.save(certificate);

        } catch (Exception e) {
            log.error("Error in findOrCreateAndUploadCertificate: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

}