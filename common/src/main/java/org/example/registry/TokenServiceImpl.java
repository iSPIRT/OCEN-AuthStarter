package org.example.registry;

import org.example.util.PropertyConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class TokenServiceImpl implements TokenService {


    private final String clientId;
    private final String clientSecret;

    private final String tokenGenerationUrl;
    private final WebClient webClient;
    private final Retry retrySpec;


    public TokenServiceImpl(@Value(PropertyConstants.CLIENT_ID) String clientId,
                            @Value(PropertyConstants.CLIENT_SECRET) String clientSecret,
                            @Value(PropertyConstants.OCEN_TOKEN_GEN_URL) String tokenGenerationUrl) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenGenerationUrl = tokenGenerationUrl;
        WebClient.Builder webcliBuilder = WebClient.builder();
        Duration timeoutDuration = Duration.ofSeconds(10);
        retrySpec = Retry.backoff(3, Duration.ofSeconds(2));
        webClient = webcliBuilder.build().mutate()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().responseTimeout(timeoutDuration).wiretap(true)))
                .build();
    }

    public Mono<Token> GetBearerToken() {
        return webClient.post()
                .uri(tokenGenerationUrl)
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret))
                .retrieve()
                .bodyToMono(Token.class)
                .retryWhen(retrySpec);
    }
}
