package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.example.dto.heartbeat.HeartbeatEventRequest;
import org.example.dto.heartbeat.HeartbeatEventType;
import org.example.dto.heartbeat.HeartbeatResponse;
import org.example.dto.journey.*;
import org.example.dto.registry.ProductNetworkDetail;
import org.example.dto.registry.Token;
import org.example.heartbeat.HeartbeatService;
import org.example.jws.SignatureService;
import org.example.registry.RegistryService;
import org.example.registry.TokenService;
import org.example.util.JsonUtil;
import org.example.util.PayloadUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.util.HeaderConstants.*;
import static org.example.util.PropertyConstants.CREATE_LOAN_APPLICATION_REQUEST_JSON;

@Service
@Log4j2
public class LoanAgentServiceImpl implements LoanAgentService{
    public static final String LENDER_CREATE_LOAN_APPLICATION_REQUEST_URL = "/v4.0.0alpha/loanApplications/createLoanRequest";

    private final HeartbeatService heartbeatService;
    private final TokenService tokenService;
    private final SignatureService signatureService;
    private final RegistryService registryService;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;


    public LoanAgentServiceImpl(HeartbeatService heartbeatService,
                               TokenService tokenService, SignatureService signatureService,
                               RegistryService registryService) {
        this.heartbeatService = heartbeatService;
        this.tokenService = tokenService;
        this.signatureService = signatureService;
        this.webClient = WebClient.create();
        this.registryService = registryService;
        this.objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public Mono<String> triggerLoanApplicationRequest() {
        //1. Generate Bearer Token
        Mono<Token> tokenMono = tokenService.GetBearerToken();
        //2. Load Payload from file
        CreateLoanApplicationRequest request = getLoanApplicationRequestPayload();
        //3. Generate Payload Signature
        String requestBodySignature = null;
        try {
            requestBodySignature = signatureService.generateParticipantSignature(objectMapper.writeValueAsString(request));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        //4. Get Product Network details by product network id
        Mono<ProductNetworkDetail> productNetworkDetailMono = registryService.getProductNetworkParticipantsByNetworkID(
                request.getProductData().getProductNetworkId());

        //5. Call all the lenders that are part of the product network and send heartbeat event
        String finalRequestBodySignature = requestBodySignature;
        Mono<List<OcenAckResponse>> lenderAckResponseListMono = Mono.zip(tokenMono, productNetworkDetailMono)
                .flatMapMany(tuples -> {
                    Token token = tuples.getT1();
                    ProductNetworkDetail productNetworkDetail = tuples.getT2();

                    return Flux.fromIterable(productNetworkDetail.getLenders())
                            // For each lender, make an asynchronous API call
                            .flatMap(lender -> webClient.post()
                                    .uri(lender.getBaseUrl() + LENDER_CREATE_LOAN_APPLICATION_REQUEST_URL)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .header(X_JWS_SIGNATURE, finalRequestBodySignature)
                                    .header(AUTHORIZATION, BEARER + token.getAccessToken())
                                    .body(BodyInserters.fromValue(request))
                                    .retrieve()
                                    .onStatus(HttpStatus::isError, response -> {
                                        int statusCode = response.statusCode().value();
                                        return response.bodyToMono(String.class)
                                                .flatMap(errorMessage -> {
                                                    log.error("Error while calling create loan application api for lender - {}, Error - {}", lender.getId(), errorMessage);
                                                    sendHeartbeatsForCreateLoanRequest(request, statusCode, errorMessage, lender.getId());
                                                    return Mono.error(new RuntimeException(errorMessage)); // Return an error Mono
                                                });
                                    })
                                    .bodyToMono(OcenAckResponse.class)
                                    .doOnNext(ocenAckResponse -> {
                                        log.info("Successfully created loan application for lender - {}. Status Code: 200, Response: {}", lender.getId(), ocenAckResponse);
                                        sendHeartbeatsForCreateLoanRequest(request, 200, ocenAckResponse.toString(), lender.getId());
                                    }));

                }).collectList();

        lenderAckResponseListMono.subscribe(
                lenderAckResponseList -> log.info("Lender Acknowledgement ResponseList - {}", lenderAckResponseList),
                throwable -> log.info("Error in lenderAckResponseListMono Subscription - {}", throwable.getMessage()),
                Mono::empty);

        return Mono.just("Loan Application Request Sent");
    }

    @Override
    public Mono<OcenAckResponse> sendLoanApplicationAsyncResponse(@RequestBody CreateLoanApplicationResponse response) {
        log.info("Loan Application Async Response received From Lender - {}", response);

        sendHeartbeatsForCreateLoanResponseAck(response);

        return Mono.just(OcenAckResponse.builder()
                .traceId(response.getMetadata().getTraceId())
                .timestamp(LocalDateTime.now().toString())
                .build());
    }

    private CreateLoanApplicationRequest getLoanApplicationRequestPayload() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CREATE_LOAN_APPLICATION_REQUEST_JSON)) {
            assert inputStream != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String requestPayload = reader.lines()
                        .collect(Collectors.joining(System.lineSeparator()));
                return objectMapper.readValue(requestPayload, CreateLoanApplicationRequest.class);
            }
        } catch (Exception e) {
            log.error("Error while reading " + CREATE_LOAN_APPLICATION_REQUEST_JSON + " file, Error - ", e);
        }
        return null;
    }

    private void sendHeartbeatsForCreateLoanResponseAck(CreateLoanApplicationResponse response) {
        for(LoanApplication loanApplication : response.getLoanApplications()) {
            sendHeartBeatEvent(HeartbeatEventType.CREATE_LOAN_APPLICATIONS_RESPONSE_ACK, response.getProductData(),
                    loanApplication.getLoanApplicationId(), 200, "Success", response.getMetadata().getOriginatorParticipantId());
        }
    }

    private void sendHeartbeatsForCreateLoanRequest(CreateLoanApplicationRequest request, Integer responseCode, String responseMessage,
                                                    String roleId) {
        for(LoanApplication loanApplication : request.getLoanApplications()) {
            sendHeartBeatEvent(HeartbeatEventType.CREATE_LOAN_APPLICATION_REQUEST, request.getProductData(),
                    loanApplication.getLoanApplicationId(), responseCode, responseMessage, roleId);
        }
    }

    private void sendHeartBeatEvent(HeartbeatEventType heartbeatEventType, ProductData productData,
                                    String loanApplicationId, Integer responseCode, String responseMessage,
                                    String roleId) {
        HeartbeatEventRequest heartbeatEventRequest = PayloadUtil.buildHeartbeatEvent(heartbeatEventType, productData,
                loanApplicationId, responseCode, responseMessage, roleId);
        log.info("Sending Heartbeat Event - {}",heartbeatEventType);
        Mono<HeartbeatResponse> heartbeatResponseMono = heartbeatService.sendHeartbeat(heartbeatEventRequest);
        heartbeatResponseMono.subscribe(
                t -> log.info("HeartbeatResponse - {}", JsonUtil.toJson(t)),
                throwable -> log.info("Error in {} heartbeat event mono Subscription - {}", heartbeatEventRequest, throwable.getMessage()),
                Mono::empty);
    }
}
