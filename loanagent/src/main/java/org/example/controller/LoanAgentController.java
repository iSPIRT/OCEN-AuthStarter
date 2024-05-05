package org.example.controller;

import lombok.extern.log4j.Log4j2;
import org.example.dto.journey.LenderAckResponse;
import org.example.dto.registry.ProductNetworkDetail;
import org.example.dto.registry.Token;
import org.example.heartbeat.HeartbeatEvent;
import org.example.heartbeat.HeartbeatEventType;
import org.example.heartbeat.HeartbeatResponse;
import org.example.heartbeat.HeartbeatService;
import org.example.jws.SignatureService;
import org.example.registry.RegistryService;
import org.example.registry.TokenService;
import org.example.util.JsonUtil;
import org.example.util.PayloadUtil;
import org.example.util.PropertyConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
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
    private final RegistryService registryService;

    private final WebClient webClient;

    private final String productId;
    private final String productNetworkId;

    public LoanAgentController(HeartbeatService heartbeatService,
                               @Value(PropertyConstants.PRODUCT_ID) String productId,
                               @Value(PropertyConstants.PRODUCT_NETWORK_ID) String productNetworkId,
                               TokenService tokenService, SignatureService signatureService,
                               RegistryService registryService) {
        this.heartbeatService = heartbeatService;
        this.productId = productId;
        this.productNetworkId = productNetworkId;
        this.tokenService = tokenService;
        this.signatureService = signatureService;
        this.webClient = WebClient.create();
        this.registryService = registryService;
    }

    @GetMapping("/loan-agent/trigger/loanApplicationRequest")
    public Mono<String> triggerLoanApplicationRequest() {
        //1. Generate Own Bearer Token
        Mono<Token> tokenMono = tokenService.GetBearerToken();
        //2. Load Payload from file
        String loanApplicationRequestPayload = getLoanApplicationRequestPayload();
        //3. Generate Payload Signature
        String requestBodySignature = signatureService.generateParticipantSignature(loanApplicationRequestPayload);

        //4. Get Product Network details by product network id
        Mono<ProductNetworkDetail> productNetworkDetailMono = registryService.getProductNetworkParticipantsByNetworkID(productNetworkId);

        //5. Call all the lenders which are part of product network
        Mono<List<LenderAckResponse>> lenderAckResponseListMono = Mono.zip(tokenMono, productNetworkDetailMono)
                .flatMapMany(tuples -> {
                    Token token = tuples.getT1();
                    ProductNetworkDetail productNetworkDetail = tuples.getT2();

                    return Flux.fromIterable(productNetworkDetail.getLenders())
                            // For each lender, make an asynchronous API call
                            .flatMap(lender -> webClient.post()
                                    .uri(lender.getBaseUrl() + LENDER_CREATE_LOAN_URL)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .header(X_JWS_SIGNATURE, requestBodySignature)
                                    .header(AUTHORIZATION, BEARER + token.getAccessToken())
                                    .body(BodyInserters.fromValue(loanApplicationRequestPayload))
                                    .retrieve()
                                    .bodyToMono(LenderAckResponse.class)
                                    .doOnError(error -> {
                                        log.error("Error while calling create loan application api for lender - {}", lender.getId());
                                    }));

                }).collectList();

        lenderAckResponseListMono.subscribe(
                lenderAckResponseList -> log.info("Lender Acknowledgement ResponseList - {}", lenderAckResponseList),
                throwable -> log.info("Error in lenderAckResponseListMono Subscription - {}", throwable.getMessage()),
                Mono::empty);

        //6. Send heartbeat event
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