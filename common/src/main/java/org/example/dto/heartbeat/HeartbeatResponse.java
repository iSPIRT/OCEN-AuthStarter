package org.example.dto.heartbeat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HeartbeatResponse {

    private String message;
    private Long timestamp;
    private String errorCode;
}
