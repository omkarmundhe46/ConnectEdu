package com.campusconnect.eventservice.controller;

import com.campusconnect.eventservice.client.UserClient;
import com.campusconnect.eventservice.dto.ParticipantResponseDto;
import com.campusconnect.eventservice.dto.UserDto;
import com.campusconnect.eventservice.service.ParticipantService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
@RestController
@RequestMapping("/api/clubs/{clubId}/events/{eventId}/participants")
@RequiredArgsConstructor
public class ExcelExportController {

    private final ParticipantService participantService;
    private final UserClient userClient; // 👈 fetch user details here

    @GetMapping("/excel")
    @PreAuthorize("hasAnyAuthority('ROLE_CLUB_ADMIN', 'ROLE_COLLEGE_ADMIN')")
    public ResponseEntity<byte[]> downloadParticipantsExcel(@PathVariable Long clubId,
                                                            @PathVariable Long eventId) throws Exception {

        if (isClubAdmin()) {
            validateClubOwnership(clubId);
        }

        List<ParticipantResponseDto> participants =
                participantService.getParticipantsByClubAndEvent(clubId, eventId);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Participants");

        // Header
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Participant ID");
        header.createCell(1).setCellValue("User ID");
        header.createCell(2).setCellValue("Name");
        header.createCell(3).setCellValue("Email");
        header.createCell(4).setCellValue("Phone");
        header.createCell(5).setCellValue("Registered At");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        int rowIdx = 1;
        for (ParticipantResponseDto p : participants) {
            Row row = sheet.createRow(rowIdx++);

            // fetch user details for each participant
            UserDto user = userClient.getUserById(p.getUserId());

            row.createCell(0).setCellValue(p.getId());
            row.createCell(1).setCellValue(p.getUserId());
            row.createCell(2).setCellValue(user.getName());
            row.createCell(3).setCellValue(user.getEmail());
//            row.createCell(4).setCellValue(user.getPhone() != null ? user.getPhone() : "");
            row.createCell(5).setCellValue(
                    p.getRegisteredAt() != null ? p.getRegisteredAt().format(dtf) : ""
            );
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=participants.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(out.toByteArray());
    }



    // ... inside your ExcelExportController class

    // ... inside your ExcelExportController class

    private void validateClubOwnership(Long clubId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();

        // AND APPLY THE SAME FIX HERE
        Object managedClubIdObj = jwt.getClaim("managedClubId");
        Long managedClubId = null;
        if (managedClubIdObj instanceof Number) {
            managedClubId = ((Number) managedClubIdObj).longValue();
        }

        if (managedClubId == null || !managedClubId.equals(clubId)) {
            throw new AccessDeniedException("You are not the admin of this club.");
        }
    }

    private boolean isClubAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_CLUB_ADMIN"));
    }
}
