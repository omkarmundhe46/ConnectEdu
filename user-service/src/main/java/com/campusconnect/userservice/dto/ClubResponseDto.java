//package com.campusconnect.userservice.dto;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.time.LocalDateTime;
//
//import jakarta.validation.Valid;
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//public class ClubResponseDto {
//    private Long id;
//    private String name;
//    private String description;
//    private Long adminId;
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
//
//    public @Valid ClubRequestDto toRequestDto() {
//        return ClubRequestDto.builder()
//                .name(this.name)
//                .description(this.description)
//                .adminId(this.adminId)
//                .build();
//    }
//}
