package org.example.registry;

import org.example.util.PropertyConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class TokenServiceImpl implements TokenService {


    private final String clientId;
    private final String clientSecret;

    private final String tokenGenerationUrl;
    private final WebClient webClient;


    public TokenServiceImpl(@Value(PropertyConstants.CLIENT_ID) String clientId,
                            @Value(PropertyConstants.CLIENT_SECRET) String clientSecret,
                            @Value(PropertyConstants.OCEN_TOKEN_GEN_URL) String tokenGenerationUrl) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenGenerationUrl = tokenGenerationUrl;
        WebClient.Builder webcliBuilder = WebClient.builder();
        webClient = webcliBuilder.build();
    }

    public Mono<Token> GetBearerToken() {
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
