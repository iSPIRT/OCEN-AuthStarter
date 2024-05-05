package org.example.registry;


import lombok.extern.log4j.Log4j2;
import org.example.dto.registry.ParticipantDetail;
import org.example.dto.registry.ProductNetworkDetail;
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

    public static final String PARTICIPANT_ROLES_URL = "/participant-roles/%s";
    public static final String PRODUCT_NETWORKS_URL = "/product-network/%s/participants";

    private final WebClient webClient;
    private final String ocenRegistryBaseUrl;
    private final TokenService tokenService;
    private final Retry retrySpec;

    public RegistryServiceImpl(@Value(PropertyConstants.OCEN_REGISTRY_BASE_URL) String ocenRegistryBaseUrl, TokenService tokenService) {

        this.ocenRegistryBaseUrl = ocenRegistryBaseUrl;
        this.tokenService = tokenService;

        WebClient.Builder webcliBuilder = WebClient.builder();
        Duration timeoutDuration = Duration.ofSeconds(10);
        retrySpec = Retry.backoff(3, Duration.ofSeconds(2));
        webClient = webcliBuilder.build().mutate()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().responseTimeout(timeoutDuration)))
                .build();
    }

    @Override
    public Mono<ParticipantDetail> getParticipantDetailByParticipantId(String participantId) {
        return tokenService.GetBearerToken()
                .flatMap(token -> {
                    log.info("Token - " + token);
                    return webClient.get().uri(ocenRegistryBaseUrl + String.format(PARTICIPANT_ROLES_URL, participantId))
                            .header("Authorization", "Bearer " + token.getAccessToken())
                            .retrieve()
                            .bodyToMono(ParticipantDetail.class)
                            .retryWhen(retrySpec);
                });
    }

    @Override
    public Mono<ProductNetworkDetail> getProductNetworkParticipantsByNetworkID(String productNetworkId) {
        return tokenService.GetBearerToken()
                .flatMap(token -> {
                    log.info("Token - " + token);
                    return webClient.get().uri(ocenRegistryBaseUrl + String.format(PRODUCT_NETWORKS_URL, productNetworkId))
                            .header("Authorization", "Bearer " + token.getAccessToken())
                            .retrieve()
                            .bodyToMono(ProductNetworkDetail.class)
                            .retryWhen(retrySpec);
                });
    }
}
