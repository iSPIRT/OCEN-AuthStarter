package org.example.registry;

import reactor.core.publisher.Mono;

public interface RegistryService {
    Mono<ParticipantDetail> getEntity(String entityId);

}