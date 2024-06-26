package org.example.dto.journey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OcenAckResponse {
    private String error;
    private String traceId;
    private String timestamp;
}
