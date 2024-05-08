package org.example.util;

import org.example.dto.heartbeat.HeartbeatEvent;
import org.example.dto.heartbeat.HeartbeatEventRequest;
import org.example.dto.heartbeat.HeartbeatEventType;
import org.example.dto.heartbeat.Recipient;
import org.example.dto.journey.ProductData;

import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PayloadUtil {
    public static HeartbeatEventRequest buildHeartbeatEvent(HeartbeatEventType eventType, ProductData productData,
                                                            String requestId, String loanApplicationId, Integer responseCode,
                                                            String responseMessage, String roleId) {
        long timestamp = new Date().getTime();

        HeartbeatEvent.LoanMetadata loanMetaData = new HeartbeatEvent.LoanMetadata();
        loanMetaData.setLoanApplicationId(loanApplicationId);
        loanMetaData.setProductData(productData);

        HeartbeatEvent heartbeatEvent = new HeartbeatEvent();
        heartbeatEvent.setTimestamp(timestamp);
        heartbeatEvent.setEventType(eventType);
        heartbeatEvent.setRecipients(List.of(Recipient.builder()
                        .roleId(roleId)
                        .responseCode(responseCode)
                        .responseMessage(responseMessage)
                .build()));
        heartbeatEvent.setLoanMetaData(loanMetaData);
        heartbeatEvent.setRequestId(requestId);

        return new HeartbeatEventRequest(heartbeatEvent);
    }

    public static String getParticipantIdFromToken(String bearerToken) {
        String token = bearerToken.replace("Bearer ", "");
        String[] chunks = token.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String payload = new String(decoder.decode(chunks[1]));
        Map<String, Object> map = JsonUtil.fromJson(payload);
        return map.get("participantId").toString();
    }
}
