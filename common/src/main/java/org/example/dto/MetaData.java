package org.example.dto;

import lombok.Data;

@Data
public class MetaData {
    private String version;
    private String originatorOrgId;
    private String originatorParticipantId;
    private String timestamp;
    private String traceId;
    private String requestId;
}
