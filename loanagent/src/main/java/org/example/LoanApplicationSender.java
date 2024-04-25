package org.example;

import org.example.jws.JWSSigner;
import org.example.registry.ParticipantDetail;
import org.example.registry.RegistryService;
import org.example.registry.RegistryServiceImpl;
import org.example.util.PropertyConstants;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.lang.JoseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.io.*;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.example.jws.JWSResponseValidator.parseSign;
import static org.example.util.PropertyConstants.LOAN_AGENT_CREDENTIALS_FILE;

public class LoanApplicationSender {

    public static final String LENDER_URL = "http://localhost:8084/loanApplications/createLoanRequest";
    public static final String X_JWS_SIGNATURE = "x-jws-signature";

    private static final WebClient WEB_CLIENT = WebClient.create();
    private static final JWSSigner JWS_SIGNER = signer();
    private static final Properties APPLICATION_PROPERTIES = loadProperties("application.properties");
    private static final RegistryService REGISTRY_SERVICE = new RegistryServiceImpl(getProperty(PropertyConstants.CLIENT_ID),
           getProperty(PropertyConstants.CLIENT_SECRET), getProperty(PropertyConstants.OCEN_TOKEN_GEN_URL), getProperty(PropertyConstants.OCEN_REGISTRY_PARTICIPANT_ROLE_URL));

    private static final String LENDER_PARTICIPANT_ID = getProperty(PropertyConstants.LENDER_PARTICIPANT_ID);

    private static String getProperty(String property){
        return APPLICATION_PROPERTIES.getProperty(PropertyConstants.getPropertyName(property));
    }

    public static JWSSigner signer()  {
        String LAJwkKeySet = getLAPublicPrivateKeyPairSet();
        try {
            return new JWSSigner(LAJwkKeySet.replaceAll("[\\s\\n]", ""));
        } catch (JoseException e) {
            throw new RuntimeException(e);
        }
    }

    private static Properties loadProperties(String filename) {
        Properties properties = new Properties();
        try (InputStream inputStream = LoanAgentApplication.class.getClassLoader().getResourceAsStream(filename)) {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new RuntimeException("Unable to find file: " + filename);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading properties file", e);
        }
        return properties;
    }

    private static String getLAPublicPrivateKeyPairSet() {
        try (InputStream inputStream = LoanAgentApplication.class.getClassLoader().getResourceAsStream(LOAN_AGENT_CREDENTIALS_FILE)) {
            assert inputStream != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                return reader.lines()
                        .collect(Collectors.joining(System.lineSeparator()));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while reading " + LOAN_AGENT_CREDENTIALS_FILE + " file, Error - ", e);
        }
    }

    public static void main(String[] args) {
        String requestBody = "{\"loanAmount\":10000,\"duration\":12,\"interestRate\":5.5}";

        // 1. Generate the signature of the body
        String requestBodySignature = getSignature(requestBody);
        System.out.println("requestBodySignature - " + requestBodySignature);

        // 2. Call the Lender and get the response
        Map<String, String> responseMap = callLender(requestBodySignature, requestBody);
        System.out.println("responseMap - " + responseMap);

        // 3. Validate the Signature
        String signatureValidation = validateResponseSignature(responseMap.get("responseBody"),
                responseMap.get(X_JWS_SIGNATURE));
        System.out.println(signatureValidation);
    }

    private static Map<String, String> callLender(String requestBodySignature, String requestBody) {
        return WEB_CLIENT.post()
                .uri(LENDER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.put(X_JWS_SIGNATURE, Collections.singletonList(requestBodySignature)))
                .body(BodyInserters.fromValue(requestBody))
                .exchange()
                .flatMap(clientResponse -> {
                    // Retrieve the response body
                    Mono<String> responseBodyMono = clientResponse.bodyToMono(String.class);

                    // Retrieve the x-jws-signature response headers
                    String responseBodySignature = clientResponse.headers().asHttpHeaders().getFirst(X_JWS_SIGNATURE);

                    return responseBodyMono.map(responseBody4 -> Map.of(X_JWS_SIGNATURE, responseBodySignature, "responseBody", responseBody4));
                })
                .block();
    }

    private static String validateResponseSignature(String body, String signature) {
        return REGISTRY_SERVICE.getEntity(LENDER_PARTICIPANT_ID)
                .map(ParticipantDetail::getPublicKey)
                .map(publicKeyCertificate -> {
                    try {
                        publicKeyCertificate = publicKeyCertificate.replaceAll("[\\s\\n]", "");
                        parseSign(signature, body.getBytes(), JsonWebKey.Factory.newJwk(publicKeyCertificate));
                    } catch (JoseException e) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Signature");
                    } catch (Exception e) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request");
                    }
                    return "valid Signature";
                }).block();
    }

    private static String getSignature(String body) {
        String signature;
        try {
            signature = JWS_SIGNER.sign(body.getBytes());
        } catch (JoseException e) {
            throw new RuntimeException(e);
        }
        return signature;
    }
}
