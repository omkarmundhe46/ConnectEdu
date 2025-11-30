package com.campusconnect.eventservice.repository;

import com.campusconnect.eventservice.dto.ChartDataDto;
import com.campusconnect.eventservice.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    List<Event> findByDateBefore(LocalDateTime date);

    List<Event> findByDateBetween(LocalDateTime start, LocalDateTime end);

    List<Event> findByDateAfterOrderByDateAsc(LocalDateTime date);
    List<Event> findByDateBeforeAndCompletedTrue(LocalDateTime date);

    List<Event> findByNameContainingIgnoreCase(String name);




    // --- ANALYTICS QUERIES ---

    // 1. Total Events per Month (Current Year)
    // Returns: [["JAN", 5], ["FEB", 3]]
    // Note: Query syntax depends on DB (MySQL uses MONTHNAME)
    @Query("SELECT new com.campusconnect.eventservice.dto.ChartDataDto(MONTHNAME(e.date), CAST(COUNT(e) AS double)) " +
            "FROM Event e " +
            "WHERE YEAR(e.date) = YEAR(CURRENT_DATE) " +
            "GROUP BY MONTH(e.date), MONTHNAME(e.date) " +
            "ORDER BY MONTH(e.date)")
    List<ChartDataDto> findEventsCountByMonth();

    // 2. Top Clubs by Participation (Global)
    // We only have clubId, so we return ID as string. Service will map to Name.
    @Query("SELECT new com.campusconnect.eventservice.dto.ChartDataDto(CAST(e.clubId AS string), CAST(COUNT(p) AS double)) " +
            "FROM Event e JOIN e.participants p " +
            "GROUP BY e.clubId " +
            "ORDER BY COUNT(p) DESC")
    List<ChartDataDto> findTopClubsByParticipation();

    // 3. Total Revenue (Global)
    // Sum of fees for all registered participants
    @Query("SELECT SUM(e.fee) FROM Event e JOIN e.participants p")
    Double getTotalRevenue();

    // --- CLUB SPECIFIC QUERIES ---

    // 4. Participation per Event (Specific Club) - Limit to last 5 events
    @Query("SELECT new com.campusconnect.eventservice.dto.ChartDataDto(e.name, CAST(COUNT(p) AS double)) " +
            "FROM Event e LEFT JOIN e.participants p " +
            "WHERE e.clubId = :clubId " +
            "GROUP BY e.id, e.name, e.date " +
            "ORDER BY e.date DESC LIMIT 5")
    List<ChartDataDto> findParticipationByEventForClub(@Param("clubId") Long clubId);

    // 5. Revenue for a Specific Club
    @Query("SELECT SUM(e.fee) FROM Event e JOIN e.participants p WHERE e.clubId = :clubId")
    Double getRevenueForClub(@Param("clubId") Long clubId);
}