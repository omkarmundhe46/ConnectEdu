//package com.campusconnect.notificationservice.entity;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.hibernate.annotations.CreationTimestamp;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "notification_requests")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class NotificationRequest {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "request_id", unique = true, nullable = false)
//    private String requestId;
//
//    @Column(name = "notification_type", nullable = false)
//    private String notificationType;
//
//    @Column(name = "user_id", nullable = false)
//    private Long userId;
//
//    @Column(name = "event_id")
//    private Long eventId;  // ✅ Added
//    
//    @Column(name = "processed_count")
//    private Integer processedCount;
//
//    @CreationTimestamp
//    @Column(name = "created_at", updatable = false)
//    private LocalDateTime createdAt;
//}

package com.campusconnect.notificationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", unique = true, nullable = false)
    private String requestId;

    @Column(name = "notification_type", nullable = false)
    private String notificationType;

    @Column(name = "processed_count")
    private int processedCount;

    @Column(name = "user_id")   // ✅ needed for participation, user-registered etc.
    private Long userId;

    @Column(name = "event_id")  // ✅ needed for event participation
    private Long eventId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
