package com.campusconnect.eventservice.repository;

import com.campusconnect.eventservice.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByClubId(Long clubId);
    Optional<Event> findByIdAndClubId(Long id, Long clubId);
    List<Event> findByDateBeforeAndCompletedFalse(LocalDateTime date);
    List<Event> findByDateLessThanEqualAndCompletedFalse(LocalDateTime dateTime);

    // ADD THIS METHOD: To find events that ended on or before a specific date
    List<Event> findByDateBefore(LocalDateTime date);

    // ADD THIS METHOD: Finds events between the start and end of a given day.
    List<Event> findByDateBetween(LocalDateTime start, LocalDateTime end);

    // ADD THIS METHOD
    List<Event> findByDateAfterOrderByDateAsc(LocalDateTime date);
}