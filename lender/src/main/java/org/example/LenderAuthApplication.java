package org.example;

import lombok.extern.log4j.Log4j2;
import org.example.filter.request.JwsRequestWebFilter;
import org.example.jws.JWSSigner;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.util.pattern.PathPattern;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import org.example.registry.RegistryService;

import static org.example.util.PropertyConstants.LENDER_CREDENTIALS_FILE;
import static org.example.util.PropertyConstants.LOAN_AGENT_PARTICIPANT_ID;


@SpringBootApplication
@ComponentScan(basePackages = {"org.example"})
@Log4j2
public class LenderAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(LenderAuthApplication.class, args);
    }

    @Bean
    JwsRequestWebFilter JwsRequestWebFilter(JWSSigner signer,
                                            @Qualifier("pathPatterns") List<PathPattern> requestPathPatterns,
                                            @Value(LOAN_AGENT_PARTICIPANT_ID) String laParticipantId,
                                            RegistryService registryService) {
        return new JwsRequestWebFilter(signer, requestPathPatterns, registryService, laParticipantId);
    }

    @Bean
    public JWSSigner signer() throws JoseException {
        String LenderJwkKeySet = getLenderPublicPrivateKeyPairSet();
        return new JWSSigner(LenderJwkKeySet.replaceAll("[\\s\\n]", ""));
    }

    private String getLenderPublicPrivateKeyPairSet() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(LENDER_CREDENTIALS_FILE)) {
            assert inputStream != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                return reader.lines()
                        .collect(Collectors.joining(System.lineSeparator()));
            }
        } catch (Exception e) {
            log.error("Error while reading " + LENDER_CREDENTIALS_FILE + " file, Error - ", e);
        }
        return null;
    }
}
