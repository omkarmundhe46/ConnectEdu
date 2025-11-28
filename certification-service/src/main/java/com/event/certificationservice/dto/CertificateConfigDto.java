package com.event.certificationservice.dto;

import com.event.certificationservice.enums.CertificateTemplateType;
import lombok.Data;

@Data
public class CertificateConfigDto {
    private Long eventId;
    private CertificateTemplateType templateType;
    private String authorityName1;
    private String signatureUrl1;
    private String authorityName2;
    private String signatureUrl2;
    private String authorityName3;
    private String signatureUrl3;
    private String authorityName4;
    private String signatureUrl4;
}