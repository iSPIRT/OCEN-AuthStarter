package org.example.controller;

import lombok.extern.log4j.Log4j2;
import org.example.heartbeat.HeartbeatEvent;
import org.example.heartbeat.HeartbeatEventType;
import org.example.heartbeat.HeartbeatResponse;
import org.example.heartbeat.HeartbeatService;
import org.example.registry.RegistryService;
import org.example.util.HeartbeatUtil;
import org.example.util.JsonUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;

@RestController
@RequestMapping("/")
@Log4j2
public class LenderController {

    private final HeartbeatService heartbeatService;
    private final String productId;
    private final String productNetworkId;


    public LenderController(HeartbeatService heartbeatService,
                            @Value("${product.id}") String productId,
                            @Value("${product.network.id}") String productNetworkId) {
        this.heartbeatService = heartbeatService;
        this.productId = productId;
        this.productNetworkId = productNetworkId;
    }

    @PostMapping("/loanApplications/createLoanRequest")
    public Mono<String> createLoanApplication(@RequestBody String body) {
        HeartbeatEvent heartbeatEvent = HeartbeatUtil.buildHeartbeatEvent(HeartbeatEventType.LOAN_REQ, productId, productNetworkId);
        System.out.println("Sending Heartbeat Event");
        Mono<HeartbeatResponse> heartbeatResponseMono = heartbeatService.sendHeartbeat(heartbeatEvent);
        heartbeatResponseMono.subscribe(t -> System.out.println(JsonUtil.toJson(t)),
                Throwable::printStackTrace,
                () -> System.out.println("completed without a value"));
        return Mono.just("Loan Application created");
    }
}
