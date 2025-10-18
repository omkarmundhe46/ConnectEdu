package com.campusconnect.clubservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

import com.campusconnect.clubservice.entity.ClubMember.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubMemberAddedRequest implements Serializable {
    private Long userId;
    private Long clubId;
    private String role;

    // ADD THESE FIELDS
    private String userName;
    private String userEmail;

    private String clubName;

//    private String requestId;   // unique request id for idempotency
//    private Map<String, Object> variables; // dynamic variables like clubName, role etc.
}
