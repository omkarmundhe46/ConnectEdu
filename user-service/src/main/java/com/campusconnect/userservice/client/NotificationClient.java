//package com.campusconnect.userservice.client;
//
//import com.campusconnect.userservice.dto.EmailSendRequest;
//import com.campusconnect.userservice.dto.SendResponse;
//import com.campusconnect.userservice.dto.UserRegisteredRequest;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//
//@FeignClient(name = "notification-service", url = "${NOTIFICATION_SERVICE_URL:http://localhost:8085}/api/notifications")
//public interface NotificationClient {
//    @PostMapping("/user-registered")
//    void notifyUserRegistered(@RequestBody UserRegisteredRequest request);
//}

package com.campusconnect.userservice.client;

import com.campusconnect.userservice.dto.UserRegisteredRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "notification-service",
    url = "${NOTIFICATION_SERVICE_URL}"
)
public interface NotificationClient {

    @PostMapping("/api/notifications/user-registered")
    void notifyUserRegistered(@RequestBody UserRegisteredRequest request);
}
