package org.example;

import lombok.extern.log4j.Log4j2;
import org.example.jws.JWSSigner;
import org.jose4j.lang.JoseException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.example.util.PropertyConstants.LOAN_AGENT_CREDENTIALS_FILE;


@SpringBootApplication
@ComponentScan(basePackages = {"org.example"})
@Log4j2
public class LoanAgentApplication {
    public static void main(String[] args) {
        SpringApplication.run(LoanAgentApplication.class, args);
    }

    @Bean
    public JWSSigner signer() throws JoseException {
        return new JWSSigner(getLAPublicPrivateKeyPairSet().replaceAll("[\\s\\n]", ""));
    }

    private String getLAPublicPrivateKeyPairSet() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(LOAN_AGENT_CREDENTIALS_FILE)) {
            assert inputStream != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                return reader.lines()
                        .collect(Collectors.joining(System.lineSeparator()));
            }
        } catch (Exception e) {
            log.error("Error while reading " + LOAN_AGENT_CREDENTIALS_FILE + " file, Error - ", e);
        }
        return null;
    }
}
