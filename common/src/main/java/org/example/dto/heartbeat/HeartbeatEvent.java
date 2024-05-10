package org.example.dto.heartbeat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.dto.journey.ProductData;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HeartbeatEvent {

    private Long timestamp;
    private HeartbeatEventType eventType;

    private String requestId;

    private LoanMetadata loanMetaData;

    private List<Recipient> recipients;

    @Data
    public static class LoanMetadata {
        private String loanApplicationId;
        private String loanId;
        private ProductData productData;
    }
}
