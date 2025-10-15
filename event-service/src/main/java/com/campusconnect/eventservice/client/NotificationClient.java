////package com.campusconnect.eventservice.client;
////
////import com.campusconnect.eventservice.dto.EmailSendRequest;
////import com.campusconnect.eventservice.dto.EventCreatedRequest;
////import com.campusconnect.eventservice.dto.SendResponse;
////import org.springframework.cloud.openfeign.FeignClient;
////import org.springframework.web.bind.annotation.PostMapping;
////import org.springframework.web.bind.annotation.RequestBody;
////
////@FeignClient(name = "notification-service", url = "${NOTIFICATION_SERVICE_URL:http://localhost:8085}/api/notifications")
////public interface NotificationClient {
////    @PostMapping("/event-created")
////    void notifyEventCreated(@RequestBody EventCreatedRequest request);
////}
//
//package com.campusconnect.eventservice.client;
//
//import com.campusconnect.eventservice.dto.EventCreatedRequest;
//import com.campusconnect.eventservice.dto.EventParticipationDTO;
//import com.campusconnect.eventservice.dto.NotificationResponse;
//
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//
//@FeignClient(
//    name = "notification-service", 
//    url = "${NOTIFICATION_SERVICE_URL:http://localhost:8085}/api/notifications"
//)
//public interface NotificationClient {
//
//    @PostMapping("/event-created")
//    void notifyEventCreated(@RequestBody EventCreatedRequest request);
//
//    @PostMapping("/event-participation")
//    NotificationResponse notifyEventParticipation(@RequestBody EventParticipationDTO request);
//}


package com.campusconnect.eventservice.client;

import com.campusconnect.eventservice.dto.EventResponseDto;
import com.campusconnect.eventservice.dto.EventParticipationDTO;
import com.campusconnect.eventservice.dto.NotificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "notification-service",
    url = "${NOTIFICATION_SERVICE_URL:http://localhost:8085}/api/notifications"
)
public interface NotificationClient {

    @PostMapping("/event-created")
    NotificationResponse notifyEventCreated(@RequestBody EventResponseDto request);

    @PostMapping("/event-participation")
    NotificationResponse notifyEventParticipation(@RequestBody EventParticipationDTO request);
}
