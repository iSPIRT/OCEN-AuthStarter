package com.example.ocenauthentication.controller;

import com.example.ocenauthentication.jws.JWSSigner;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/")
@Log4j2
public class OcenController
{
    @PostMapping("/loanApplications/createLoanRequest")
    public Mono<String> createLoanApplication(@RequestBody String body){
        return Mono.just("Loan Application created");
    }

    @PostMapping("/generate-signature")
    Mono<String> generateSignature(@RequestBody String body,
                                   @RequestHeader("JWK_KEY_SET") String keyset) {
        return Mono.fromCallable(() -> {
            try {
                JWSSigner jwsSigner = new JWSSigner(keyset);
                String signature = jwsSigner.sign(body);
                log.debug("body:{}, signature:{}", body, signature);
                return signature;
            } catch (Exception e) {
                return e.getMessage();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
