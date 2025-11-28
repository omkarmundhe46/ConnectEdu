package com.event.certificationservice.service;

import com.event.certificationservice.dto.CertificateConfigDto;
import com.event.certificationservice.dto.CertificateNotificationRequest;
import com.event.certificationservice.dto.EventResponseDto;
import com.event.certificationservice.dto.UserResponseDto;
import com.event.certificationservice.entity.Certificate;
import com.event.certificationservice.entity.EventCertificateConfig;
import com.event.certificationservice.enums.CertificateTemplateType;
import com.event.certificationservice.repository.CertificateRepository;
import com.event.certificationservice.client.UserClient;
import com.event.certificationservice.client.EventClient;
import com.event.certificationservice.repository.EventCertificateConfigRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
    private final EventCertificateConfigRepository configRepository; // INJECT

    public void saveConfig(Long eventId, CertificateTemplateType templateType) {
        EventCertificateConfig config = configRepository.findByEventId(eventId)
                .orElse(new EventCertificateConfig());
        config.setEventId(eventId);
        config.setTemplateType(templateType);
        configRepository.save(config);
    }

    // --- NEW: GET CONFIGURATION ---
    public EventCertificateConfig getConfig(Long eventId) {
        return configRepository.findByEventId(eventId).orElse(null);
    }

    /**
     * Generate PDF bytes using JasperReports with DYNAMIC TEMPLATE.
     */
    public byte[] generateCertificatePdf(Long eventId, Long userId) throws JRException {
        try {
            UserResponseDto user = userClient.getUserById(userId);
            EventResponseDto event = eventClient.getEventById(eventId);

            // 1. Get Config
            EventCertificateConfig config = configRepository.findByEventId(eventId)
                    .orElseThrow(() -> new RuntimeException("No certificate template selected for event " + eventId));

            CertificateTemplateType template = config.getTemplateType();

            // 2. LOAD TEMPLATE FILES
// The Enum already contains the full path (e.g., "certificates/...")
            ClassPathResource jrxmlRes = new ClassPathResource(template.getJrxmlPath());
            ClassPathResource bgRes = new ClassPathResource(template.getBackgroundPath());

            try (InputStream jrxmlStream = jrxmlRes.getInputStream();
                 InputStream bgStream = bgRes.getInputStream()) {

                JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);

                Map<String, Object> params = new HashMap<>();
                params.put("userName", user.getName());
                params.put("eventName", event.getName());
                params.put("eventDate", event.getDate().toString());

                // Pass the background image stream
                params.put("backgroundImage", bgStream);

                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
                return JasperExportManager.exportReportToPdf(jasperPrint);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load template files", e);
        }
    }

    private InputStream getResourceStream(String classpathPath) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(classpathPath);
        if (is == null) {
            throw new IllegalArgumentException("Resource not found in classpath: " + classpathPath);
        }
        return is;
    }


    public Certificate generateSaveAndSend(Long eventId, Long userId) {

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

    public byte[] getCertificateForDownload(Long eventId, Long userId) {
        // Step 1: Check the business rule (is it after 7 PM on the event day?)
        EventResponseDto event = eventClient.getEventById(eventId);
        LocalDate eventDate = event.getDate();
        LocalDateTime activationTime = LocalDateTime.of(eventDate, LocalTime.of(16, 0));

        if (LocalDateTime.now().isBefore(activationTime)) {
            throw new IllegalStateException("Certificate is not yet available for download. Please check back after 7 PM on the event date.");
        }

        // 1. Ensure the certificate exists and is on S3 by calling the helper.
        Certificate certificate = findOrCreateAndUploadCertificate(eventId, userId, null);

        // 2. Download the now-guaranteed-to-exist file from S3.
        return s3StorageService.downloadFile(certificate.getFilePath());
    }


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