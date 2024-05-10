package org.example.service;

import org.example.dto.journey.CreateLoanApplicationRequest;
import org.example.dto.journey.OcenAckResponse;
import reactor.core.publisher.Mono;

public interface LenderService {
    Mono<OcenAckResponse> sendLoanApplicationResponse(CreateLoanApplicationRequest request);
}
