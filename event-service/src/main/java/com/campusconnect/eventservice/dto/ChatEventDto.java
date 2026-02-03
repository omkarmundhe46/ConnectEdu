package com.campusconnect.eventservice.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatEventDto {
    private String name;
    private String date;
    private String time;
    private String location;
    private String clubName;
    private String contactName1;
    private String contactPhone1;
    private String contactName2;
    private String contactPhone2;
}