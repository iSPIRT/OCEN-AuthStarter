package org.example.controller;

import lombok.extern.log4j.Log4j2;
import org.example.jws.SignatureService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/common")
@Log4j2
public class CommonController {

    private final SignatureService signatureService;

    public CommonController(SignatureService signatureService) {
        this.signatureService = signatureService;
    }

    @PostMapping("/generate-signature")
    Mono<String> generateSignature(@RequestBody String body,
                                   @RequestHeader("JWK_KEY_SET") String keyset) {
        return Mono.fromCallable(() -> signatureService.generateSignature(body, keyset))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/heartbeat")
    Mono<String> checkCredit() {
        return Mono.just("Running");
    }
}
