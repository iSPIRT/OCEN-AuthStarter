package org.example.controller;

import lombok.extern.log4j.Log4j2;
import org.example.dto.LenderAckResponse;
import org.example.heartbeat.HeartbeatEvent;
import org.example.heartbeat.HeartbeatEventType;
import org.example.heartbeat.HeartbeatResponse;
import org.example.heartbeat.HeartbeatService;
import org.example.jws.SignatureService;
import org.example.registry.Token;
import org.example.registry.TokenService;
import org.example.util.JsonUtil;
import org.example.util.PayloadUtil;
import org.example.util.PropertyConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.example.util.HeaderConstants.*;
import static org.example.util.PropertyConstants.CREATE_LOAN_APPLICATION_REQUEST_JSON;

@RestController
@RequestMapping("/")
@Log4j2
public class LoanAgentController {
    public static final String LENDER_CREATE_LOAN_URL = "http://localhost:8084/v4.0.0alpha/loanApplications/createLoanRequest";

    private final HeartbeatService heartbeatService;
    private final TokenService tokenService;
    private final SignatureService signatureService;

    private final WebClient webClient;

    private final String productId;
    private final String productNetworkId;

    public LoanAgentController(HeartbeatService heartbeatService,
                               @Value(PropertyConstants.PRODUCT_ID) String productId,
                               @Value(PropertyConstants.PRODUCT_NETWORK_ID) String productNetworkId,
                               TokenService tokenService, SignatureService signatureService) {
        this.heartbeatService = heartbeatService;
        this.productId = productId;
        this.productNetworkId = productNetworkId;
        this.tokenService = tokenService;
        this.signatureService = signatureService;
        this.webClient = WebClient.create();
    }

    @PostMapping("/mock-request/loan-agent/sendLoanApplicationRequest")
    public Mono<String> sendLoanApplicationRequest(@RequestBody String body) {
        //1. Generate Own Bearer Token
        Mono<Token> tokenMono = tokenService.GetBearerToken();
        //2. Load Payload from file
        String loanApplicationRequestPayload = getLoanApplicationRequestPayload();
        //3. Generate Payload Signature
        String requestBodySignature = signatureService.generateParticipantSignature(loanApplicationRequestPayload);

        //4. Hit Lender Create Loan Request API
        Mono<LenderAckResponse> lenderAckResponseMono = tokenMono.flatMap(token -> webClient.post()
                .uri(LENDER_CREATE_LOAN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(X_JWS_SIGNATURE, requestBodySignature)
                .header(AUTHORIZATION, BEARER + token.getAccessToken())
                .body(BodyInserters.fromValue(loanApplicationRequestPayload))
                .retrieve()
                .bodyToMono(LenderAckResponse.class));

        lenderAckResponseMono.subscribe(lenderAckResponse -> log.info("Lender Acknowledgement Response - {}", lenderAckResponse),
                throwable -> log.info("Error in lenderAckResponseMono Subscription - {}", throwable.getMessage()), Mono::empty);

        //5. Send heartbeat event
        sendHeartBeatEvent(HeartbeatEventType.CREATE_LOAN_APPLICATION_REQUEST);

        return Mono.just("Loan Application Request Sent");
    }

    @PostMapping("/v4.0.0alpha/loanApplications/createLoanResponse")
    public void sendLoanApplicationAsyncResponse(@RequestBody String body) {
        log.info("Loan Application Async Response received From Lender - {}", body);

        sendHeartBeatEvent(HeartbeatEventType.CREATE_LOAN_APPLICATIONS_RESPONSE_ACK);
    }

    private String getLoanApplicationRequestPayload() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CREATE_LOAN_APPLICATION_REQUEST_JSON)) {
            assert inputStream != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                return reader.lines()
                        .collect(Collectors.joining(System.lineSeparator()));
            }
        } catch (Exception e) {
            log.error("Error while reading " + CREATE_LOAN_APPLICATION_REQUEST_JSON + " file, Error - ", e);
        }
        return null;
    }

    private void sendHeartBeatEvent(HeartbeatEventType heartbeatEventType) {
        HeartbeatEvent heartbeatEvent = PayloadUtil.buildHeartbeatEvent(heartbeatEventType, productId, productNetworkId);
        log.info("Sending Heartbeat Event - {}",heartbeatEventType);
        Mono<HeartbeatResponse> heartbeatResponseMono = heartbeatService.sendHeartbeat(heartbeatEvent);
        heartbeatResponseMono.subscribe(
                t -> log.info("HeartbeatResponse - {}", JsonUtil.toJson(t)),
                throwable -> log.info("Error in {} heartbeat event mono Subscription - {}", heartbeatEvent, throwable.getMessage()),
                Mono::empty);
    }
}
