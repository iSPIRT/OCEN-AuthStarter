package org.example.controller;

import lombok.extern.log4j.Log4j2;
import org.example.dto.CreateLoanApplicationRequest;
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
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static org.example.util.HeaderConstants.*;
import static org.example.util.PropertyConstants.CREATE_LOAN_APPLICATION_RESPONSE_JSON;

@RestController
@RequestMapping("/")
@Log4j2
public class LenderController {

    public static final String LA_CREATE_LOAN_APPLICATION_RESPONSE_URL = "http://localhost:8085/v4.0.0alpha/loanApplications/createLoanResponse";

    private final HeartbeatService heartbeatService;
    private final String productId;
    private final String productNetworkId;
    private final TokenService tokenService;
    private final SignatureService signatureService;

    private final WebClient webClient;


    public LenderController(HeartbeatService heartbeatService,
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

    @PostMapping("/v4.0.0alpha/loanApplications/createLoanRequest")
    public Mono<LenderAckResponse> sendLoanApplicationResponse(@RequestBody CreateLoanApplicationRequest createLoanApplicationRequest) {
        //1. Send heartbeat event
        sendHeartBeatEvent(HeartbeatEventType.CREATE_LOAN_APPLICATION_REQUEST_ACK);

        //2. Process request and send async response to LA
        sendAsyncResponseToLA();

        //3. Send request acknowledgement response
        return Mono.just(LenderAckResponse.builder()
                        .traceId(createLoanApplicationRequest.getMetadata().getTraceId())
                        .timestamp(LocalDateTime.now().toString())
                .build());
    }

    private void sendAsyncResponseToLA() {
        //1. Generate Own Bearer Token
        Mono<Token> tokenMono = tokenService.GetBearerToken();
        //2. Load Payload from file
        String loanApplicationResponsePayload = getLoanApplicationResponsePayload();
        //3. Generate Payload Signature
        String signature = signatureService.generateParticipantSignature(loanApplicationResponsePayload);

        //4. Hit LoanAgent API
        Mono<String> laCreateLoanResponseMono = tokenMono.flatMap(token -> webClient.post()
                .uri(LA_CREATE_LOAN_APPLICATION_RESPONSE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(X_JWS_SIGNATURE, signature)
                .header(AUTHORIZATION, BEARER + token.getAccessToken())
                .body(BodyInserters.fromValue(loanApplicationResponsePayload))
                .retrieve()
                .bodyToMono(String.class));

        laCreateLoanResponseMono.subscribe(
                s -> Mono.empty(),
                throwable -> log.info("Error in laCreateLoanResponseMono Subscription - {}", throwable.getMessage()),
                () -> log.info("Create Loan Application Async Response Sent")
        );

        //5. Send heartbeat event
        sendHeartBeatEvent(HeartbeatEventType.CREATE_LOAN_APPLICATIONS_RESPONSE);
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
