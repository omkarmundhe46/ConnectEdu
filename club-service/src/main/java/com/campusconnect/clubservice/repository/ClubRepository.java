package com.campusconnect.clubservice.repository;

import com.campusconnect.clubservice.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {
    boolean existsByName(String name);
    List<Club> findByNameContainingIgnoreCase(String name);
}