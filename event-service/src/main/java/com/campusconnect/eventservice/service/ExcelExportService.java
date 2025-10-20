package com.campusconnect.eventservice.service;

import com.campusconnect.eventservice.client.UserClient;
import com.campusconnect.eventservice.dto.EventResponseDto;
import com.campusconnect.eventservice.dto.ParticipantResponseDto;
import com.campusconnect.eventservice.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Use constructor injection
public class ExcelExportService {

    private final UserClient userClient;
    private final ParticipantService participantService;
    private final EventService eventService;

    public byte[] generateParticipantsExcel(Long clubId, Long eventId) throws Exception {
        // 1. Get Event and Participant data
        EventResponseDto event = eventService.getClubEventById(clubId, eventId);
        List<ParticipantResponseDto> participants = participantService.getParticipantsByClubAndEvent(clubId, eventId);

        // 2. Efficiently fetch all user details in one call
        List<Long> userIds = participants.stream().map(ParticipantResponseDto::getUserId).collect(Collectors.toList());
        Map<Long, UserDto> userMap = userClient.getUsersByIds(userIds).stream()
                .collect(Collectors.toMap(UserDto::getId, user -> user));

        // 3. Build the Excel Workbook
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Participants for " + event.getName());

        // Header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // Define Columns
        String[] columns = {
                "Participant ID", "User ID", "Name", "Email", "Department",
                "College", "Mobile Number", "Address", "Transaction ID", "Registered At"
        };
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // Populate Data Rows
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        int rowIdx = 1;
        for (ParticipantResponseDto p : participants) {
            UserDto user = userMap.get(p.getUserId()); // Fast lookup from map
            Row row = sheet.createRow(rowIdx++);

            row.createCell(0).setCellValue(p.getId());
            row.createCell(1).setCellValue(p.getUserId());
            row.createCell(2).setCellValue(user != null ? user.getName() : "N/A");
            row.createCell(3).setCellValue(user != null ? user.getEmail() : "N/A");
            row.createCell(4).setCellValue(user != null ? user.getDepartment() : "N/A");
            row.createCell(5).setCellValue(p.getCollege());
            row.createCell(6).setCellValue(p.getMobileNumber());
            row.createCell(7).setCellValue(p.getAddress());
            row.createCell(8).setCellValue(p.getPaymentId());
            row.createCell(9).setCellValue(p.getRegisteredAt() != null ? p.getRegisteredAt().format(dtf) : "");
        }

        // Auto-size columns for readability
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }
}