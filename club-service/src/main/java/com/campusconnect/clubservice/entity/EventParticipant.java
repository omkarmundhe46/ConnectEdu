//package com.campusconnect.clubservice.entity;
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
//@Table(name = "event_participants")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class EventParticipant {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "user_id", nullable = false)
//    private Long userId;
//
//    @Column(name = "event_id", nullable = false)
//    private Long eventId;
//
//    @CreationTimestamp
//    @Column(name = "registered_at", updatable = false)
//    private LocalDateTime registeredAt;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "event_id", insertable = false, updatable = false)
//    private Event event;
//}