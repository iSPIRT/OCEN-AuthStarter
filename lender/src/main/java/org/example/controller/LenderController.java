package org.example.controller;

import org.example.dto.journey.CreateLoanApplicationRequest;
import org.example.dto.journey.OcenAckResponse;
import org.example.service.LenderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/")
public class LenderController {

    private final LenderService lenderService;

    public LenderController(LenderService lenderService) {
        this.lenderService = lenderService;
    }

    @PostMapping("/v4.0.0alpha/loanApplications/createLoanRequest")
    public Mono<OcenAckResponse> sendLoanApplicationResponse(@RequestBody CreateLoanApplicationRequest request) {
        return lenderService.sendLoanApplicationResponse(request);
    }
}
