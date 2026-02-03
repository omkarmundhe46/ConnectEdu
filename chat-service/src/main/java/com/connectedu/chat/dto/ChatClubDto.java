package com.connectedu.chat.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatClubDto {
    private String name;
    private String description;
    private String adminEmail; // Useful for "Contact" questions

}