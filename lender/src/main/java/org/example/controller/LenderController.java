package org.example.controller;

import lombok.extern.log4j.Log4j2;
import org.example.heartbeat.HeartbeatEvent;
import org.example.heartbeat.HeartbeatEventType;
import org.example.heartbeat.HeartbeatResponse;
import org.example.heartbeat.HeartbeatService;
import org.example.jws.SignatureService;
import org.example.registry.Token;
import org.example.registry.TokenService;
import org.example.util.PayloadUtil;
import org.example.util.JsonUtil;
import org.example.util.PropertyConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.example.util.PropertyConstants.CREATE_LOAN_APPLICATION_RESPONSE_JSON;
import static org.example.util.PropertyConstants.LENDER_CREDENTIALS_FILE;

@RestController
@RequestMapping("/")
@Log4j2
public class LenderController {

    private final HeartbeatService heartbeatService;
    private final String productId;
    private final String productNetworkId;
    private final TokenService tokenService;
    private final SignatureService signatureService;


    public LenderController(HeartbeatService heartbeatService,
                            @Value(PropertyConstants.PRODUCT_ID) String productId,
                            @Value(PropertyConstants.PRODUCT_NETWORK_ID) String productNetworkId,
                            TokenService tokenService, SignatureService signatureService) {
        this.heartbeatService = heartbeatService;
        this.productId = productId;
        this.productNetworkId = productNetworkId;
        this.tokenService = tokenService;
        this.signatureService = signatureService;
    }

    @PostMapping("/loanApplications/createLoanRequest")
    public Mono<String> createLoanApplication(@RequestBody String body) {
        HeartbeatEvent heartbeatEvent = PayloadUtil.buildHeartbeatEvent(HeartbeatEventType.CREATE_LOAN_APPLICATION_REQUEST_ACK, productId, productNetworkId);
        System.out.println("Sending Heartbeat Event");
        Mono<HeartbeatResponse> heartbeatResponseMono = heartbeatService.sendHeartbeat(heartbeatEvent);
        heartbeatResponseMono.subscribe(t -> System.out.println(JsonUtil.toJson(t)),
                Throwable::printStackTrace,
                () -> System.out.println("completed without a value"));
        return Mono.just("Loan Application created");
    }

    @GetMapping("/mock-request/lender/sendLoanApplicationResponse")
    public Mono<String> sendLoanApplicationResponse(@RequestBody String body) {
        //1. Generate Own Bearer Token
        Mono<Token> tokenMono = tokenService.GetBearerToken();
        //2. Load Payload from file
        String loanApplicationResponsePayload = getLoanApplicationResponsePayload();
        //3. Generate Payload Signature
        String signature = signatureService.generateParticipantSignature(loanApplicationResponsePayload);
        //4. Hit LoanAgent API

        //5.
        HeartbeatEvent heartbeatEvent = PayloadUtil.buildHeartbeatEvent(HeartbeatEventType.CREATE_LOAN_APPLICATIONS_RESPONSE, productId, productNetworkId);
        System.out.println("Sending Heartbeat Event");
        Mono<HeartbeatResponse> heartbeatResponseMono = heartbeatService.sendHeartbeat(heartbeatEvent);
        heartbeatResponseMono.subscribe(t -> System.out.println(JsonUtil.toJson(t)),
                Throwable::printStackTrace,
                () -> System.out.println("completed without a value"));
        return Mono.just("Loan Application Response Sent");
    }

    private String getLoanApplicationResponsePayload() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CREATE_LOAN_APPLICATION_RESPONSE_JSON)) {
            assert inputStream != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                return reader.lines()
                        .collect(Collectors.joining(System.lineSeparator()));
            }
        } catch (Exception e) {
            log.error("Error while reading " + CREATE_LOAN_APPLICATION_RESPONSE_JSON + " file, Error - ", e);
        }
        return null;
    }
}
