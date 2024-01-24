package com.example.ocenauthentication;

import com.example.ocenauthentication.filter.request.JwsRequestWebFilter;
import com.example.ocenauthentication.jws.JWSSigner;
import com.example.ocenauthentication.registry.RegistryService;
import com.google.common.io.Resources;
import lombok.extern.log4j.Log4j2;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.util.pattern.PathPattern;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.ocenauthentication","com.example.ocenauthentication.controller"})
@Log4j2
public class OcenAuthenticationApplication {

	private static final String LENDER_CREDENTIALS_FILE = "lender_private_public_keypair_set.json";


	public static void main(String[] args) {
		SpringApplication.run(OcenAuthenticationApplication.class, args);
	}

	@Bean
	JwsRequestWebFilter JwsRequestWebFilter(JWSSigner signer,
											@Qualifier("pathPatterns") List<PathPattern> requestPathPatterns,
											@Value("${la.participant.id}") String laParticipantId,
											RegistryService registryService) {
		return new JwsRequestWebFilter(signer, requestPathPatterns, registryService, laParticipantId);
	}

	@Bean
	public JWSSigner signer() throws JoseException {
		String LenderJwkKeySet = getLenderPublicPrivateKeyPairSet();

		return new JWSSigner(LenderJwkKeySet.replaceAll("[\\s\\n]", ""));
	}

	private String getLenderPublicPrivateKeyPairSet()  {
		try {
			URL url = Resources.getResource(LENDER_CREDENTIALS_FILE);
			return Files.readString(Paths.get(url.toURI()));
		}catch (Exception e){
			log.error("Error while reading " + LENDER_CREDENTIALS_FILE + " file, Error - ", e);
		}
		return null;
	}

}
