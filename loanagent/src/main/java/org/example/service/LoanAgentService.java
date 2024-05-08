package org.example.service;

import org.example.dto.journey.CreateLoanApplicationResponse;
import org.example.dto.journey.OcenAckResponse;
import reactor.core.publisher.Mono;

public interface LoanAgentService {
    Mono<String> triggerLoanApplicationRequest();

    Mono<OcenAckResponse> sendLoanApplicationAsyncResponse(CreateLoanApplicationResponse response);
}
