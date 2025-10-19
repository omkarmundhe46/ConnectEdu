//package com.campusconnect.eventservice.dto; // or notificationservice.dto
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class EventCreatedNotificationRequest {
//
//    private Long eventId;
//    private String eventName;
//    private LocalDateTime eventDate;
//    private String eventLocation;
//    private Long clubId;
//    private String clubName;
//    private List<RecipientDto> recipients;
//
//    @Data
//    @Builder
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class RecipientDto {
//        private Long userId;
//        private String name;
//        private String email;
//    }
//}