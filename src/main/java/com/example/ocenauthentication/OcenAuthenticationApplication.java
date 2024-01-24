package com.example.ocenauthentication;

import com.example.ocenauthentication.filter.request.JwsRequestWebFilter;
import com.example.ocenauthentication.jws.JWSSigner;
import com.example.ocenauthentication.registry.RegistryService;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.util.pattern.PathPattern;

import java.util.List;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.ocenauthentication","com.example.ocenauthentication.controller"})
public class OcenAuthenticationApplication {

	public static void main(String[] args) {
		SpringApplication.run(OcenAuthenticationApplication.class, args);
	}

	@Bean
	JwsRequestWebFilter JwsRequestWebFilter(JWSSigner signer,
											@Qualifier("pathPatterns") List<PathPattern> requestPathPatterns,
											RegistryService registryService) {
		return new JwsRequestWebFilter(signer, requestPathPatterns, registryService);
	}

	@Bean
	public JWSSigner signer() throws JoseException {
		String LenderJwkKeySet = "<LENDER_JWK_KEY_PAIR_SET>";
		return new JWSSigner(LenderJwkKeySet.replaceAll("[\\s\\n]", ""));
	}

}
