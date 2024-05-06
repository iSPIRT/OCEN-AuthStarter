package org.example.heartbeat;

import reactor.core.publisher.Mono;

public interface HeartbeatService {

    Mono<HeartbeatResponse> sendHeartbeat(HeartbeatEvent event);
}
