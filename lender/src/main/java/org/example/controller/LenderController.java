package org.example.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/")
@Log4j2
public class LenderController {

    @PostMapping("/loanApplications/createLoanRequest")
    public Mono<String> createLoanApplication(@RequestBody String body){
        return Mono.just("Loan Application created");
    }
}
