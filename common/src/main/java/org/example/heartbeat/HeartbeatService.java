package org.example.heartbeat;

import org.example.dto.heartbeat.HeartbeatEventRequest;
import org.example.dto.heartbeat.HeartbeatResponse;
import reactor.core.publisher.Mono;

public interface HeartbeatService {

    Mono<HeartbeatResponse> sendHeartbeat(HeartbeatEventRequest heartbeatEventRequest);
}
