package com.event.certificationservice.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum CertificateTemplateType {

    // --- CODING CLUB TEMPLATES ---
    CODING_MODERN(
            "Coding Modern",
            "certificates/coding_club/Coding_Club_Temp1.jrxml",
            "certificates/coding_club/Coding_Club_Temp1.png",
            "https://connectedu-media.s3.ap-south-1.amazonaws.com/coding_club_temp1.png",
            "CODING" // <--- New Category Field
    ),
    CODING_ARTISTIC(
            "Coding Artistic",
            "certificates/coding_club/Coding_Club_Temp2.jrxml",
            "certificates/coding_club/Coding_Club_Temp2.png",
            "https://connectedu-media.s3.ap-south-1.amazonaws.com/coding_club_temp2.png",
            "CODING"
    ),

    // --- SPORTS CLUB TEMPLATES ---
    SPORTS_CLASSIC(
            "Sports Chess",
            "certificates/sport_club/Sport_Club_Chess.jrxml",
            "certificates/sport_club/Sport_Club_Chess.png",
            "https://connectedu-media.s3.ap-south-1.amazonaws.com/sport_club_chess_s3.png",
            "SPORTS"
    );            // ),

    // --- GENERIC TEMPLATES (For any club) ---
//    GENERIC_SIMPLE(
//            "Simple Certificate",
//            "certificates/generic/Generic_Temp1.jrxml",
//            "certificates/generic/Generic_Temp1.png",
//            "https://connectedu-media.s3.ap-south-1.amazonaws.com/generic_temp1.png",
//            "ALL"
//    );

    private final String displayName;
    private final String jrxmlPath;
    private final String backgroundPath;
    private final String previewUrl;
    private final String category; // New Field

    CertificateTemplateType(String displayName, String jrxmlPath, String backgroundPath, String previewUrl, String category) {
        this.displayName = displayName;
        this.jrxmlPath = jrxmlPath;
        this.backgroundPath = backgroundPath;
        this.previewUrl = previewUrl;
        this.category = category;
    }

    // Add getter
    public String getCategory() { return category; }

    // ... existing getters ...
    public String getName() { return name(); }
    public String getDisplayName() { return displayName; }
    public String getJrxmlPath() { return jrxmlPath; }
    public String getBackgroundPath() { return backgroundPath; }
    public String getPreviewUrl() { return previewUrl; }
}