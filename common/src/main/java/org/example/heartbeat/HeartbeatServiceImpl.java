package org.example.heartbeat;

import org.example.registry.Token;
import org.example.util.JsonUtil;
import org.example.util.PropertyConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class HeartbeatServiceImpl implements HeartbeatService {

    private final WebClient webClient;

    private final String clientId;
    private final String clientSecret;
    private final String tokenGenerationUrl;
    private final String heartbeatEventUrl;

    public HeartbeatServiceImpl(@Value(PropertyConstants.CLIENT_ID) String clientId,
                                @Value(PropertyConstants.CLIENT_SECRET) String clientSecret,
                                @Value(PropertyConstants.OCEN_TOKEN_GEN_URL) String tokenGenerationUrl,
                                @Value(PropertyConstants.OCEN_HEARTBEAT_EVENT_URL) String heartbeatEventUrl) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenGenerationUrl = tokenGenerationUrl;
        this.heartbeatEventUrl = heartbeatEventUrl;

        WebClient.Builder webcliBuilder = WebClient.builder();
        webClient = webcliBuilder.build();
    }

    @Override
    public Mono<HeartbeatResponse> sendHeartbeat(HeartbeatEvent event) {
        return getBearerToken(clientId, clientSecret)
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

    private Mono<Token> getBearerToken(String clientId, String clientSecret) {
        return webClient.post()
                .uri(tokenGenerationUrl)
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret))
                .retrieve()
                .bodyToMono(Token.class);
    }
}
