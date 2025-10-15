package com.campusconnect.eventservice.service;

import com.campusconnect.eventservice.client.UserClient;
import com.campusconnect.eventservice.dto.ParticipantResponseDto;
import com.campusconnect.eventservice.dto.UserDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {

    private final UserClient userClient;

    public ExcelExportService(UserClient userClient) {
        this.userClient = userClient;
    }

    public byte[] generateParticipantsExcel(List<ParticipantResponseDto> participants,
                                            String clubName,
                                            String eventName) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Participants");

        // Header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        int rowIdx = 0;

        // Title row with Event + Club
        Row titleRow = sheet.createRow(rowIdx++);
        titleRow.createCell(0).setCellValue("Club: " + clubName + " | Event: " + eventName);

        rowIdx++; // blank line

        // Column headers
        String[] columns = {"Participant ID", "User ID", "Name", "Email", "Department", "Registered At"};
        Row headerRow = sheet.createRow(rowIdx++);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (ParticipantResponseDto p : participants) {
            // Fetch user details by userId
            UserDto user = null;
            try {
                user = userClient.getUserById(p.getUserId());
            } catch (Exception e) {
                // If user service fails, still generate row
                System.err.println("Failed to fetch user details for ID " + p.getUserId() + ": " + e.getMessage());
            }

            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(p.getId() != null ? p.getId() : 0);   // Participant ID
            row.createCell(1).setCellValue(p.getUserId() != null ? p.getUserId() : 0); // User ID
            row.createCell(2).setCellValue(user != null ? user.getName() : "");   // Name
            row.createCell(3).setCellValue(user != null ? user.getEmail() : "");  // Email
            row.createCell(4).setCellValue(user != null ? user.getDepartment() : ""); // Department
            row.createCell(5).setCellValue(
                    p.getRegisteredAt() != null ? p.getRegisteredAt().format(dtf) : ""
            );
        }

        // Auto-size columns
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }
}
