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
    private final String participantRolesUrl;
    private final TokenService tokenService;

    public RegistryServiceImpl(@Value(PropertyConstants.OCEN_REGISTRY_PARTICIPANT_ROLE_URL) String participantRolesUrl, TokenService tokenService) {

        this.participantRolesUrl = participantRolesUrl;
        this.tokenService = tokenService;

        WebClient.Builder webcliBuilder = WebClient.builder();
        webClient = webcliBuilder.build();
    }

    @Override
    public Mono<ParticipantDetail> getEntity(String entityId) {
        return tokenService.GetBearerToken()
                .flatMap(token -> {
                    System.out.println("Token - " + token);
                    return webClient.get().uri(participantRolesUrl + entityId)
                            .header("Authorization", "Bearer " + token.getAccessToken())
                            .retrieve()
                            .bodyToMono(ParticipantDetail.class);
                });
    }
}
