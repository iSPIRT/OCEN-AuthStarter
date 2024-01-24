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
		String LenderJwkKeySet = "{\n" +
				"    \"keys\": [\n" +
				"        {\n" +
				"            \"p\": \"82Xb5pstEubVZDCRXU3t1r-CaNNJ8Lv0DdSAtawqOiedVY7D8CwfO5exf3jrI-KRJbyXPYuiLw16-JzQI0DE9N3y-UoyRBZt26gOjjG3e44pWv9SyWcQl6NS-GUWv90HEY0tKNZtWAkHJ4mXCkONBTB6s3QJlgR-i2Pad0WO-dk\",\n" +
				"            \"kty\": \"RSA\",\n" +
				"            \"q\": \"2sHq8VvabefSkHw29SVon7_NU8ZRB50_JtalKJiIdGyPKft3vEu3Gvxk501mxL0NHNTVcl5kdrDsjRmkXkWVzkqflg6qpdRuj9wpASBeSnyZUof3rSbXUl-Ws63H40_9QTgDtZrew1BSGSInW6jQ-bLEKGw0Czsl0JDs5KMFmIM\",\n" +
				"            \"d\": \"HX7Znz6QvPzhhm5KO4UE0inMSPvaZqAZC3-SvKlkkmzDjEq3f6CK3fK0P0KE0NVtxSmcQPCX3kv9rgNURLHzOii5GVWMTNaDDWN5Vze_nu69uaOnSJUlPpnSgM8LU4HMMkq1Y3j8DFlRLZeiNiy-azoNidjEHO7d_oUVqz2IJNGwYOnIUoHkyLgizuHpPRM0ie6EEryEpTVzlJvdhBRh0ZRWaXHBWtS6CPYO2oDX_PyBrwqYWAY_dOxtzXPNaTkkYSq2vHdXnwMqQ67VJBqQeMEixc0EroZB7jjUf07I5RDkz6H7Jit0eR6XD6h3O0PuWuoz3j41ZSSKwpeVdfdnoQ\",\n" +
				"            \"e\": \"AQAB\",\n" +
				"            \"use\": \"sig\",\n" +
				"            \"kid\": \"ab69858a-e940-4651-91e6-0d45ad3528ce\",\n" +
				"            \"qi\": \"WVLTCtM7YTTb5Pv6Emv75GQdi5DaZAx-JZvgR_N-i4ENLOmIRx2ftWD58VP6Azi6fbbOCKAfFEL-TfuoolntabCVhFsEpCE5LTE2BDpLCHJhURv3AodGex5TTajN1kn_NBkFNl20cFF-zAHaHqER06SATecZnGV-_vSJ1FmVpTA\",\n" +
				"            \"dp\": \"7JYq_OoDEydrbtrCwbYZJK-8fFY8vhKENNlFTtINrb-J8Hs4PAE3Tr6xmt2XeqclPVAmln1e5WuuB3Ct5EZ17Cq9ndGgMIy4tlWR6GetGR1jV39tIjXba_omkSn5xf97enUG23YqjPMhkC3usdxdfBjWfA6jrr2pn6Ys2di0FGk\",\n" +
				"            \"alg\": \"RS256\",\n" +
				"            \"dq\": \"054fzxhSN1u5IDhpWcC0c0UyNcZ1AG6ndsTRxHG6HxYdcDlpXOfJ2_spCXCg59USaD-jtB2IBiGk0-JoVSHvhz49HwZVIlJcGiKaMBLF5DygfWw2tnQKfA8oU-zanxT0IzEytW157Ws34ERf2pV2gvbiN1BlEDBJpndsEsl5Uk0\",\n" +
				"            \"n\": \"z_0cbb45D2odurFLr_1ddsoikAHbKI85HdIBxPKltB-s9FQPfVRM3POOISE4jxLbb96Lcjk0eDNz6HMW3NvqE9St6yOT3s2zUB1FwxnqQiwW7XkOfrebnsv4lJA6PpzGp40vYb0bvyR0wmCfCZyin3ECXbcdoTfi10N9Vk2MfMeb4Bwn6FOP3Q9KT8_DML87avUdbSohU83O3Xv56_JAkHy1PvxXz99JWTMZDyPB5BubTfKlvV1mF8GO6Mu8cYvH55jkfhsAdW6pg-qtiQ5aNqfkM-9b0ssdbEO1OOqnByoqkdB78nF5n2V8x7dQ7UfW3G4JcnMqrbVvKJJ5iL-yCw\"\n" +
				"        }\n" +
				"    ]\n" +
				"}";
		return new JWSSigner(LenderJwkKeySet.replaceAll("[\\s\\n]", ""));
	}

}
