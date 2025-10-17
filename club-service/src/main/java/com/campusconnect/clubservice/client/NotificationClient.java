//package com.campusconnect.clubservice.client;
//
//import com.campusconnect.clubservice.dto.ClubMemberAddedRequest;
//import com.campusconnect.clubservice.dto.EmailSendRequest;
//import com.campusconnect.clubservice.dto.SendResponse;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//
//@FeignClient(
//    name = "notification-service",
//    url = "${NOTIFICATION_SERVICE_URL:http://localhost:8085}/api/notifications"
//)
//public interface NotificationClient {
//	@PostMapping("/club-member-added")
//	SendResponse notifyClubMemberAdded(@RequestBody ClubMemberAddedRequest request);
//
//}