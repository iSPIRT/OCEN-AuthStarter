package org.example.util;

import org.example.heartbeat.HeartbeatEvent;
import org.example.heartbeat.HeartbeatEventType;

import java.util.Base64;
import java.util.Date;
import java.util.Map;

public class PayloadUtil {
    public static HeartbeatEvent buildHeartbeatEvent(HeartbeatEventType eventType, String productId, String productNetworkId) {
        long timestamp = new Date().getTime();
        HeartbeatEvent.ProductData productData = new HeartbeatEvent.ProductData();
        productData.setProductId(productId);
        productData.setProductNetworkId(productNetworkId);

        HeartbeatEvent.LoanMetadata loanMetadata = new HeartbeatEvent.LoanMetadata();
        loanMetadata.setLoanApplicationId("some-application-id" + timestamp);
        loanMetadata.setLoanId("some-loan-id" + timestamp);
        loanMetadata.setProductData(productData);

        HeartbeatEvent heartbeatEvent = new HeartbeatEvent();
        heartbeatEvent.setTimestamp(timestamp);
        heartbeatEvent.setEventType(eventType);
        heartbeatEvent.setResponseCode(200);
        heartbeatEvent.setLoanMetadata(loanMetadata);
        heartbeatEvent.setRequestId("createLoanRequestSample");

        return heartbeatEvent;
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
