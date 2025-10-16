//package com.event.certificationservice.client;
//
//import com.event.certificationservice.dto.CertificateNotificationRequest;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//
//@FeignClient(name = "notification-service", url = "${NOTIFICATION_SERVICE_URL}/api/notifications")
//public interface NotificationClient {
//
//    @PostMapping("/certificate-issued")
//    void sendCertificate(@RequestBody CertificateNotificationRequest request);
//}