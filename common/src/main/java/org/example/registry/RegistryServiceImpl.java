package org.example.registry;


import lombok.extern.log4j.Log4j2;
import org.example.util.PropertyConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@Log4j2
public class RegistryServiceImpl implements RegistryService {

    private final WebClient webClient;
    private final String participantRolesUrl;
    private final TokenService tokenService;
    private final Retry retrySpec;

    public RegistryServiceImpl(@Value(PropertyConstants.OCEN_REGISTRY_PARTICIPANT_ROLE_URL) String participantRolesUrl, TokenService tokenService) {

        this.participantRolesUrl = participantRolesUrl;
        this.tokenService = tokenService;

        WebClient.Builder webcliBuilder = WebClient.builder();
        Duration timeoutDuration = Duration.ofSeconds(10);
        retrySpec = Retry.backoff(3, Duration.ofSeconds(2));
        webClient = webcliBuilder.build().mutate()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().responseTimeout(timeoutDuration)))
                .build();
    }

    @Override
    public Mono<ParticipantDetail> getEntity(String entityId) {
        return tokenService.GetBearerToken()
                .flatMap(token -> {
                    log.info("Token - " + token);
                    return webClient.get().uri(participantRolesUrl + entityId)
                            .header("Authorization", "Bearer " + token.getAccessToken())
                            .retrieve()
                            .bodyToMono(ParticipantDetail.class)
                            .retryWhen(retrySpec);
                });
    }
}
