package com.event.certificationservice.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum CertificateTemplateType {
    CODING_CLUB_TEMP1(
            "Coding Club Modern",
            "certificates/coding_club/Coding_Club_Temp1.jrxml",
            "certificates/coding_club/Coding_Club_Temp1.png",
            "https://connectedu-media.s3.ap-south-1.amazonaws.com/coding_club_temp1.png" // External URL for Admin Preview (or S3)
    ),
    CODING_CLUB_TEMP2(
            "Coding Club Artistic",
            "certificates/coding_club/Coding_Club_Temp2.jrxml",
            "certificates/coding_club/Coding_Club_Temp2.png",
            "https://connectedu-media.s3.ap-south-1.amazonaws.com/coding_club_temp2.png"
    );

    private final String displayName;
    private final String jrxmlPath;
    private final String backgroundPath;
    private final String previewUrl;

    CertificateTemplateType(String displayName, String jrxmlPath, String backgroundPath, String previewUrl) {
        this.displayName = displayName;
        this.jrxmlPath = jrxmlPath;
        this.backgroundPath = backgroundPath;
        this.previewUrl = previewUrl;
    }

    public String getName() {
        return name();
    }

    public String getDisplayName() { return displayName; }
    public String getJrxmlPath() { return jrxmlPath; }
    public String getBackgroundPath() { return backgroundPath; }
    public String getPreviewUrl() { return previewUrl; }

}