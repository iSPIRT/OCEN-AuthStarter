package org.example.controller;

import lombok.extern.log4j.Log4j2;
import org.example.jws.JWSSigner;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/common")
@Log4j2
public class CommonController {
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

    @GetMapping("/heartbeat")
    Mono<String> checkCredit() {
        return Mono.just("Running");
    }
}
