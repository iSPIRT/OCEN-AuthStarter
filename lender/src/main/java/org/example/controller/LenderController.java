package org.example.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.example.dto.heartbeat.HeartbeatEventRequest;
import org.example.dto.heartbeat.HeartbeatEventType;
import org.example.dto.heartbeat.HeartbeatResponse;
import org.example.dto.journey.*;
import org.example.dto.registry.ParticipantDetail;
import org.example.dto.registry.Token;
import org.example.heartbeat.HeartbeatService;
import org.example.jws.SignatureService;
import org.example.registry.RegistryService;
import org.example.registry.TokenService;
import org.example.util.JsonUtil;
import org.example.util.PayloadUtil;
import org.springframework.http.HttpStatus;
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

    public static final String LA_CREATE_LOAN_APPLICATION_RESPONSE_URL = "/v4.0.0alpha/loanApplications/createLoanResponse";

    private final TokenService tokenService;
    private final SignatureService signatureService;
    private final RegistryService registryService;
    private final HeartbeatService heartbeatService;

    private final ObjectMapper objectMapper;
    private final WebClient webClient;


    public LenderController(HeartbeatService heartbeatService,
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

    @PostMapping("/v4.0.0alpha/loanApplications/createLoanRequest")
    public Mono<OcenAckResponse> sendLoanApplicationResponse(@RequestBody CreateLoanApplicationRequest request) {
        //1. Send heartbeat event
        sendHeartbeatsForCreateLoanRequestAck(request);

        //2. Process request and send async response to LA
        sendAsyncResponseToLA(request);

        //3. Send request acknowledgement response
        return Mono.just(OcenAckResponse.builder()
                        .traceId(request.getMetadata().getTraceId())
                        .timestamp(LocalDateTime.now().toString())
                .build());
    }

    private void sendAsyncResponseToLA(CreateLoanApplicationRequest request) {
        //1. Generate Own Bearer Token
        Mono<Token> tokenMono = tokenService.GetBearerToken();
        //2. Load Payload from file
        CreateLoanApplicationResponse response = getLoanApplicationResponsePayload();
        //3. Generate Payload Signature
        String signature = null;
        try {
            signature = signatureService.generateParticipantSignature(objectMapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        //4. Get LA participant details
        Mono<ParticipantDetail> laParticipantDetail = registryService.getParticipantDetailByParticipantId(request.getMetadata().getOriginatorParticipantId());

        //5. Hit LoanAgent API and send Heart beat event
        String finalSignature = signature;
        Mono<OcenAckResponse> laOcenAckResponseMono = Mono.zip(tokenMono, laParticipantDetail)
                .flatMap(tuples -> {
                    Token token = tuples.getT1();
                    ParticipantDetail participantDetail = tuples.getT2();

                    return webClient.post()
                            .uri(participantDetail.getBaseUrl() + LA_CREATE_LOAN_APPLICATION_RESPONSE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(X_JWS_SIGNATURE, finalSignature)
                            .header(AUTHORIZATION, BEARER + token.getAccessToken())
                            .body(BodyInserters.fromValue(response))
                            .retrieve()
                            .onStatus(HttpStatus::isError, clientResponse -> {
                                int statusCode = clientResponse.statusCode().value();
                                return clientResponse.bodyToMono(String.class)
                                        .flatMap(errorMessage -> {
                                            log.error("Error while sending async create loan application response for la - {}, Error - {}", participantDetail.getId(), errorMessage);
                                            sendHeartbeatsForCreateLoanResponse(response, statusCode, errorMessage,
                                                    request.getMetadata().getOriginatorParticipantId());
                                            return Mono.error(new RuntimeException(errorMessage)); // Return an error Mono
                                        });
                            })
                            .bodyToMono(OcenAckResponse.class)
                            .doOnNext(ocenAckResponse -> {
                                log.info("Successfully sent loan application response for la - {}. Status Code: 200, Response: {}", participantDetail.getId(), ocenAckResponse);
                                sendHeartbeatsForCreateLoanResponse(response, 200, ocenAckResponse.toString(),
                                        request.getMetadata().getOriginatorParticipantId());
                            });
                });

        laOcenAckResponseMono.subscribe(
                s -> Mono.empty(),
                throwable -> log.info("Error in laOcenAckResponseMono Subscription - {}", throwable.getMessage()),
                () -> log.info("Create Loan Application Async Response Sent")
        );
    }


    private CreateLoanApplicationResponse getLoanApplicationResponsePayload() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CREATE_LOAN_APPLICATION_RESPONSE_JSON)) {
            assert inputStream != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String responseJson = reader.lines()
                        .collect(Collectors.joining(System.lineSeparator()));
                return objectMapper.readValue(responseJson, CreateLoanApplicationResponse.class);
            }
        } catch (Exception e) {
            log.error("Error while reading " + CREATE_LOAN_APPLICATION_RESPONSE_JSON + " file, Error - ", e);
        }
        return null;
    }

    private void sendHeartbeatsForCreateLoanRequestAck(CreateLoanApplicationRequest request) {
        for(LoanApplication loanApplication : request.getLoanApplications()) {
            sendHeartBeatEvent(HeartbeatEventType.CREATE_LOAN_APPLICATION_REQUEST_ACK, request.getProductData(), request.getMetadata().getRequestId(),
                    loanApplication.getLoanApplicationId(), 200, "Success", request.getMetadata().getOriginatorParticipantId());
        }
    }

    private void sendHeartbeatsForCreateLoanResponse(CreateLoanApplicationResponse response, Integer responseCode, String responseMessage,
                                                     String roleId) {
        for(LoanApplication loanApplication : response.getLoanApplications()) {
            sendHeartBeatEvent(HeartbeatEventType.CREATE_LOAN_APPLICATIONS_RESPONSE, response.getProductData(), response.getMetadata().getRequestId(),
                    loanApplication.getLoanApplicationId(), responseCode, responseMessage, roleId);
        }
    }

    private void sendHeartBeatEvent(HeartbeatEventType heartbeatEventType, ProductData productData, String requestId,
                                    String loanApplicationId, Integer responseCode, String responseMessage, String roleId) {
        HeartbeatEventRequest heartbeatEventRequest = PayloadUtil.buildHeartbeatEvent(heartbeatEventType, productData, requestId,
                loanApplicationId, responseCode, responseMessage, roleId);
        log.info("Sending Heartbeat Event - {}",heartbeatEventType);
        Mono<HeartbeatResponse> heartbeatResponseMono = heartbeatService.sendHeartbeat(heartbeatEventRequest);
        heartbeatResponseMono.subscribe(
                t -> log.info("HeartbeatResponse - {}", JsonUtil.toJson(t)),
                throwable -> log.info("Error in {} heartbeat event mono Subscription - {}", heartbeatEventRequest, throwable.getMessage()),
                Mono::empty);
    }
}
