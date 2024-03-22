package org.example.registry;


import org.example.util.PropertyConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class RegistryServiceImpl implements RegistryService {

    private final WebClient webClient;

    private final String clientId;
    private final String clientSecret;
    private final String tokenGenerationUrl;
    private final String participantRolesUrl;

    public RegistryServiceImpl(@Value(PropertyConstants.CLIENT_ID) String clientId,
                               @Value(PropertyConstants.CLIENT_SECRET) String clientSecret,
                               @Value(PropertyConstants.OCEN_TOKEN_GEN_URL) String tokenGenerationUrl,
                               @Value(PropertyConstants.OCEN_REGISTRY_PARTICIPANT_ROLE_URL) String participantRolesUrl) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenGenerationUrl = tokenGenerationUrl;
        this.participantRolesUrl = participantRolesUrl;

        WebClient.Builder webcliBuilder = WebClient.builder();
        webClient = webcliBuilder.build();
    }

    @Override
    public Mono<ParticipantDetail> getEntity(String entityId) {
        return getBearerToken(clientId, clientSecret)
                .flatMap(token -> {
                    System.out.println("Token - " + token);
                    return webClient.get().uri(participantRolesUrl + entityId)
                            .header("Authorization", "Bearer " + token.getAccessToken())
                            .retrieve()
                            .bodyToMono(ParticipantDetail.class);
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
