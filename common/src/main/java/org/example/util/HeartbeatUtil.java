package org.example.util;

import org.example.heartbeat.HeartbeatEvent;
import org.example.heartbeat.HeartbeatEventType;

import java.util.Date;

public class HeartbeatUtil {
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
}
