package org.example.heartbeat;

import lombok.extern.log4j.Log4j2;
import org.example.dto.heartbeat.HeartbeatEventRequest;
import org.example.dto.heartbeat.HeartbeatResponse;
import org.example.registry.TokenService;
import org.example.util.HeaderConstants;
import org.example.util.PropertyConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@Log4j2
public class HeartbeatServiceImpl implements HeartbeatService {

    private final WebClient webClient;

    private final String heartbeatEventUrl;

    private final TokenService tokenService;

    private final Retry retrySpec;

    public HeartbeatServiceImpl(@Value(PropertyConstants.OCEN_HEARTBEAT_EVENT_URL) String heartbeatEventUrl,
                                TokenService tokenService) {
        this.heartbeatEventUrl = heartbeatEventUrl;
        this.tokenService = tokenService;

        WebClient.Builder webcliBuilder = WebClient.builder();
        Duration timeoutDuration = Duration.ofSeconds(10);
        retrySpec = Retry.backoff(3, Duration.ofSeconds(2));
        webClient = webcliBuilder.build().mutate()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().responseTimeout(timeoutDuration).wiretap(true)))
                .build();
    }

    @Override
    public Mono<HeartbeatResponse> sendHeartbeat(HeartbeatEventRequest heartbeatEventRequest) {
        return tokenService.GetBearerToken()
                .flatMap(token -> {
                    log.info("Token - " + token);
                    log.info("HeartbeatEventRequest - " + heartbeatEventRequest);
                    return webClient.post().uri(heartbeatEventUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(heartbeatEventRequest)
                            .header(HeaderConstants.AUTHORIZATION, HeaderConstants.BEARER + token.getAccessToken())
                            .retrieve().onStatus(HttpStatus::is5xxServerError, t -> Mono.empty())
                            .bodyToMono(HeartbeatResponse.class)
                            .retryWhen(retrySpec);
                });
    }
}
