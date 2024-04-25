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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.example.util.PropertyConstants.CREATE_LOAN_APPLICATION_REQUEST_JSON;

@RestController
@RequestMapping("/")
@Log4j2
public class LoanAgentController {
    private final HeartbeatService heartbeatService;
    private final String productId;
    private final String productNetworkId;

    private final TokenService tokenService;

    private final SignatureService signatureService;


    public LoanAgentController(HeartbeatService heartbeatService,
                               @Value(PropertyConstants.PRODUCT_ID) String productId,
                               @Value(PropertyConstants.PRODUCT_NETWORK_ID) String productNetworkId,
                               TokenService tokenService, SignatureService signatureService) {
        this.heartbeatService = heartbeatService;
        this.productId = productId;
        this.productNetworkId = productNetworkId;
        this.tokenService = tokenService;
        this.signatureService = signatureService;
    }

    @PostMapping("/loanApplications/createLoanResponse")
    public Mono<String> createLoanApplication(@RequestBody String body){
        HeartbeatEvent heartbeatEvent = PayloadUtil.buildHeartbeatEvent(HeartbeatEventType.CREATE_LOAN_APPLICATIONS_RESPONSE_ACK, productId, productNetworkId);
        System.out.println("Sending Heartbeat Event");
        Mono<HeartbeatResponse> heartbeatResponseMono = heartbeatService.sendHeartbeat(heartbeatEvent);
        heartbeatResponseMono.subscribe(t -> System.out.println(JsonUtil.toJson(t)),
                Throwable::printStackTrace,
                () -> System.out.println("completed without a value"));
        return Mono.just("Loan Application response received");
    }

    @PostMapping("/mock-request/loan-agent/sendLoanApplicationRequest")
    public Mono<String> sendLoanApplicationRequest(@RequestBody String body) {
        //1. Generate Own Bearer Token
        Mono<Token> tokenMono = tokenService.GetBearerToken();
        //2. Load Payload from file
        String loanApplicationRequestPayload = getLoanApplicationRequestPayload();
        //3. Generate Payload Signature
        String signature = signatureService.generateParticipantSignature(loanApplicationRequestPayload);
        //4. Hit Lender Create Loan Request  API


        //5. CREATE_LOAN_APPLICATION_REQUEST_ACK
        HeartbeatEvent heartbeatEvent = PayloadUtil.buildHeartbeatEvent(HeartbeatEventType.CREATE_LOAN_APPLICATION_REQUEST, productId, productNetworkId);
        System.out.println("Sending Heartbeat Event");
        Mono<HeartbeatResponse> heartbeatResponseMono = heartbeatService.sendHeartbeat(heartbeatEvent);
        heartbeatResponseMono.subscribe(t -> System.out.println(JsonUtil.toJson(t)),
                Throwable::printStackTrace,
                () -> System.out.println("completed without a value"));
        return Mono.just("Loan Application Request Sent");
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
}
