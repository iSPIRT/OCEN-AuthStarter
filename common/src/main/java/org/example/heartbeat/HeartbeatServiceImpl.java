package org.example.heartbeat;

import org.example.registry.TokenService;
import org.example.util.JsonUtil;
import org.example.util.PropertyConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class HeartbeatServiceImpl implements HeartbeatService {

    private final WebClient webClient;

    private final String heartbeatEventUrl;

    private final TokenService tokenService;

    public HeartbeatServiceImpl(@Value(PropertyConstants.OCEN_HEARTBEAT_EVENT_URL) String heartbeatEventUrl,
                                TokenService tokenService) {
        this.heartbeatEventUrl = heartbeatEventUrl;
        this.tokenService = tokenService;

        WebClient.Builder webcliBuilder = WebClient.builder();
        webClient = webcliBuilder.build();
    }

    @Override
    public Mono<HeartbeatResponse> sendHeartbeat(HeartbeatEvent event) {
        return tokenService.GetBearerToken()
                .flatMap(token -> {
                    System.out.println("Token - " + token + " " + JsonUtil.toJson(event));
                    return webClient.post().uri(heartbeatEventUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(JsonUtil.toJson(event))
                            .header("Authorization", "Bearer " + token.getAccessToken())
                            .retrieve().onStatus(HttpStatus::is5xxServerError, t -> Mono.empty())
                            .bodyToMono(HeartbeatResponse.class);
                });
    }
}
