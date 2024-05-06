package org.example.registry;

import org.example.dto.registry.ParticipantDetail;
import org.example.dto.registry.ProductNetworkDetail;
import reactor.core.publisher.Mono;

public interface RegistryService {
    Mono<ParticipantDetail> getParticipantDetailByParticipantId(String participantId);

    Mono<ProductNetworkDetail> getProductNetworkParticipantsByNetworkID(String productNetworkId);
}