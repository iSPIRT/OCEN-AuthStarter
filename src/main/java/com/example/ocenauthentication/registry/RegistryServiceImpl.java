package com.example.ocenauthentication.registry;


import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class RegistryServiceImpl implements RegistryService {

    public static final String TOKEN_URL = "https://auth.ocen.network/realms/dev/protocol/openid-connect/token";

    private final WebClient webClient;

    private String clientId = "41efd1cd-b412-4705-9d7a-667fbc353220";
    private String clientSecret = "ckt01bM65MApQxvqoE0oZJnzXBDhzwTj";

    public RegistryServiceImpl() {
        WebClient.Builder webcliBuilder = WebClient.builder();
        webClient = webcliBuilder.build();
    }

    @Override
    public Mono<ParticipantDetail> getEntity(String entityId) {
        return getBearerToken(clientId, clientSecret)
                .flatMap(token -> {
                    System.out.println("Token - " + token);
                    return webClient.get().uri("https://dev.ocen.network/service/participant-roles/" + entityId)
                            .header("Authorization", "Bearer " + token.getAccessToken())
                            .retrieve()
                            .bodyToMono(ParticipantDetail.class);
                });
    }

    public Mono<Token> getBearerToken(String clientId, String clientSecret) {
        return webClient.post()
                .uri(TOKEN_URL)
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret))
                .retrieve()
                .bodyToMono(Token.class);
    }
}
