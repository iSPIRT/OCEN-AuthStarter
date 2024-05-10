package org.example.controller;

import lombok.extern.log4j.Log4j2;
import org.example.dto.journey.CreateLoanApplicationResponse;
import org.example.dto.journey.OcenAckResponse;
import org.example.service.LoanAgentService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/")
@Log4j2
public class LoanAgentController {

    private final LoanAgentService loanAgentService;

    public LoanAgentController(LoanAgentService loanAgentService) {
        this.loanAgentService = loanAgentService;
    }

    @GetMapping("/loan-agent/trigger/loanApplicationRequest")
    public Mono<String> triggerLoanApplicationRequest() {
        return loanAgentService.triggerLoanApplicationRequest();
    }

    @PostMapping("/v4.0.0alpha/loanApplications/createLoanResponse")
    public Mono<OcenAckResponse> sendLoanApplicationAsyncResponse(@RequestBody CreateLoanApplicationResponse response) {
        return loanAgentService.sendLoanApplicationAsyncResponse(response);
    }
}