package com.example.ocenauthentication.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/health/")
@Log4j2
public class HealthController
{

    @GetMapping("/test")
    Mono<String> checkCredit() {
        return Mono.just("Running");
    }
}
