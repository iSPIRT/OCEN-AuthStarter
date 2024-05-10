package org.example.dto.heartbeat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Recipient {

    private String roleId;
    private Integer responseCode;
    private String responseMessage;
}
