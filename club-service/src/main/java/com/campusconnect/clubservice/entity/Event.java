//package com.campusconnect.clubservice.entity;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import lombok.ToString;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Entity
//@Table(name = "events")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@ToString(exclude = {"club", "participants"})
//public class Event {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false)
//    private String name;
//
//    @Column(nullable = false, columnDefinition = "TEXT")
//    private String description;
//
//    @Column(nullable = false)
//    private LocalDateTime date;
//
//    @Column(nullable = false)
//    private String location;
//
//    @Column(name = "club_id", nullable = false)
//    private Long clubId;
//
//    @CreationTimestamp
//    @Column(name = "created_at", updatable = false)
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "club_id", insertable = false, updatable = false)
//    private Club club;
//
//    @OneToMany(mappedBy = "eventId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<EventParticipant> participants;
//}