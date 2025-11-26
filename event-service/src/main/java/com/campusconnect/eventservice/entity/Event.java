package com.campusconnect.eventservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "participants")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private Double fee = 0.0;

    @Column(nullable = false)
    private String location;

    @Column(name = "image_url") // New column for the banner image URL
    private String imageUrl;

    @Column(name = "club_id", nullable = false)
    private Long clubId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "meeting_link")
    private String meetingLink;
    
    @Column(nullable = false)
    private Boolean certificatesGenerated = false;

    
    @Column(nullable = false)
    private boolean completed = false;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "contact_name_1")
    private String contactName1;

    @Column(name = "contact_phone_1")
    private String contactPhone1;

    @Column(name = "contact_name_2")
    private String contactName2;

    @Column(name = "contact_phone_2")
    private String contactPhone2;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EventParticipant> participants;
}