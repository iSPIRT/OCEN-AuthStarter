package org.example.registry;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class RegistryServiceImpl implements RegistryService {

    private final WebClient webClient;

    private final String lenderClientId;
    private final String lenderClientSecret;
    private final String tokenGenerationUrl;
    private final String participantRolesUrl;

    public RegistryServiceImpl(@Value("${client.id}") String lenderClientId,
                               @Value("${client.secret}") String lenderClientSecret,
                               @Value("${ocen.token.generation.url}") String tokenGenerationUrl,
                               @Value("${ocen.participant.roles.url}") String participantRolesUrl) {
        this.lenderClientId = lenderClientId;
        this.lenderClientSecret = lenderClientSecret;
        this.tokenGenerationUrl = tokenGenerationUrl;
        this.participantRolesUrl = participantRolesUrl;

        WebClient.Builder webcliBuilder = WebClient.builder();
        webClient = webcliBuilder.build();
    }

    @Override
    public Mono<ParticipantDetail> getEntity(String entityId) {
        return getBearerToken(lenderClientId, lenderClientSecret)
                .flatMap(token -> {
                    System.out.println("Token - " + token);
                    return webClient.get().uri(participantRolesUrl + entityId)
                            .header("Authorization", "Bearer " + token.getAccessToken())
                            .retrieve()
                            .bodyToMono(ParticipantDetail.class);
                });
    }

    public Mono<Token> getBearerToken(String clientId, String clientSecret) {
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
