package org.example.heartbeat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HeartbeatEvent {

    private Long timestamp;
    private HeartbeatEventType eventType;

    private String requestId;

    private LoanMetadata loanMetadata;

    private Integer responseCode;

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public HeartbeatEventType getEventType() {
        return eventType;
    }

    public void setEventType(HeartbeatEventType eventType) {
        this.eventType = eventType;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public LoanMetadata getLoanMetadata() {
        return loanMetadata;
    }

    public void setLoanMetadata(LoanMetadata loanMetadata) {
        this.loanMetadata = loanMetadata;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }


    @Override
    public String toString() {
        return "HeartbeatEvent{" +
                "timestamp=" + timestamp +
                ", eventType=" + eventType +
                ", requestId='" + requestId + '\'' +
                ", loanMetadata=" + loanMetadata +
                ", responseCode=" + responseCode +
                '}';
    }

    public static class LoanMetadata {
        private String loanApplicationId;
        private String loanId;
        private ProductData productData;

        public String getLoanApplicationId() {
            return loanApplicationId;
        }

        public void setLoanApplicationId(String loanApplicationId) {
            this.loanApplicationId = loanApplicationId;
        }

        public String getLoanId() {
            return loanId;
        }

        public void setLoanId(String loanId) {
            this.loanId = loanId;
        }

        public ProductData getProductData() {
            return productData;
        }

        public void setProductData(ProductData productData) {
            this.productData = productData;
        }

        @Override
        public String toString() {
            return "LoanMetadata{" +
                    "loanApplicationId='" + loanApplicationId + '\'' +
                    ", loanId='" + loanId + '\'' +
                    ", productData=" + productData +
                    '}';
        }
    }

    public static class ProductData {
        private String productId;
        private String productNetworkId;

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getProductNetworkId() {
            return productNetworkId;
        }

        public void setProductNetworkId(String productNetworkId) {
            this.productNetworkId = productNetworkId;
        }

        @Override
        public String toString() {
            return "ProductData{" +
                    "productId='" + productId + '\'' +
                    ", productNetworkId='" + productNetworkId + '\'' +
                    '}';
        }
    }
}
