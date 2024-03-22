package org.example.heartbeat;

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return "HeartbeatResponse{" +
                "message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", errorCode='" + errorCode + '\'' +
                '}';
    }
}
