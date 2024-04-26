package org.example.controller;

import lombok.extern.log4j.Log4j2;
import org.example.heartbeat.HeartbeatEvent;
import org.example.heartbeat.HeartbeatEventType;
import org.example.heartbeat.HeartbeatResponse;
import org.example.heartbeat.HeartbeatService;
import org.example.jws.SignatureService;
import org.example.registry.ParticipantDetail;
import org.example.registry.RegistryService;
import org.example.registry.Token;
import org.example.registry.TokenService;
import org.example.util.JsonUtil;
import org.example.util.PayloadUtil;
import org.example.util.PropertyConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

import static org.example.util.PropertyConstants.CREATE_LOAN_APPLICATION_REQUEST_JSON;

@RestController
@RequestMapping("/")
@Log4j2
public class LoanAgentController {
    public static final String X_JWS_SIGNATURE = "x-jws-signature";
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String LENDER_CREATE_LOAN_URL = "http://localhost:8084/loanApplications/createLoanRequest";

    private final HeartbeatService heartbeatService;
    private final TokenService tokenService;
    private final SignatureService signatureService;
    private final RegistryService registryService;

    private final WebClient webClient;

    private final String productId;
    private final String productNetworkId;
    private final String lenderParticipantId;




    public LoanAgentController(HeartbeatService heartbeatService,
                               @Value(PropertyConstants.PRODUCT_ID) String productId,
                               @Value(PropertyConstants.PRODUCT_NETWORK_ID) String productNetworkId,
                               @Value(PropertyConstants.LENDER_PARTICIPANT_ID) String lenderParticipantId,
                               TokenService tokenService, SignatureService signatureService,
                               RegistryService registryService) {
        this.heartbeatService = heartbeatService;
        this.productId = productId;
        this.productNetworkId = productNetworkId;
        this.lenderParticipantId = lenderParticipantId;
        this.tokenService = tokenService;
        this.signatureService = signatureService;
        this.registryService = registryService;
        this.webClient = WebClient.create();
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
        String requestBodySignature = signatureService.generateParticipantSignature(loanApplicationRequestPayload);

        //4. Hit Lender Create Loan Request API
        Mono<ClientResponse> responseMono = tokenMono.flatMap(token -> webClient.post()
                .uri(LENDER_CREATE_LOAN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(X_JWS_SIGNATURE, requestBodySignature)
                .header(AUTHORIZATION, BEARER + token.getAccessToken())
                .body(BodyInserters.fromValue(loanApplicationRequestPayload))
                .exchange());

        // 5. Subscribe to the response
        responseMono.flatMap(response -> {
            // 5.1 Retrieve the response body
            Mono<String> responseBodyMono = response.bodyToMono(String.class);

            // 5.2 Retrieve headers from the response
            Mono<Map<String, String>> headersMono = Mono.just(response.headers().asHttpHeaders())
                    .map(HttpHeaders::toSingleValueMap);

            // 5.3 Combine the response body and headers
            return Mono.zip(responseBodyMono, headersMono);
        }).subscribe(tuple -> {
            String responseBody = tuple.getT1(); // Response body
            Map<String, String> headers = tuple.getT2(); // Response headers
            String signatureHeader = headers.get(X_JWS_SIGNATURE);
            log.info("Lender Response Body - {}", responseBody);

            // 5.4 Validate the response Signature with lender's public key
            validateSignature(responseBody, signatureHeader)
                    .subscribe(s -> log.info("Signature is valid"));
        });


        //6. CREATE_LOAN_APPLICATION_REQUEST_ACK
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

    private Mono<Boolean> validateSignature(String body, String signature) {
        return registryService.getEntity(lenderParticipantId)
                .map(ParticipantDetail::getPublicKey)
                .map(publicKeyCertificate -> signatureService.verifySignature(body, signature, publicKeyCertificate));
    }
}
