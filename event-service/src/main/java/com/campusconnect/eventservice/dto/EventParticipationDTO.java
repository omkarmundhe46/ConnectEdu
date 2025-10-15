//package com.campusconnect.eventservice.dto;
//
//import jakarta.validation.constraints.NotNull;
//import lombok.Data;
//
//@Data
//public class ParticipantRequestDto {
//    private Long userId;
//    private Long eventId;
//    private String eventTitle;
//    private String eventDate;
//    private String location;
//    private Long clubId;
//}

package com.campusconnect.eventservice.dto;

import lombok.Data;

@Data
public class EventParticipationDTO {
    private Long userId;
    private Long eventId;
}
