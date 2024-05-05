package org.example.registry;

import org.example.dto.registry.Token;
import reactor.core.publisher.Mono;

public interface TokenService {

    Mono<Token> GetBearerToken();

}
