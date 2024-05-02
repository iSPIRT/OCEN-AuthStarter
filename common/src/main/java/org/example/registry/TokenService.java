package org.example.registry;

import reactor.core.publisher.Mono;

public interface TokenService {

    Mono<Token> GetBearerToken();

}
