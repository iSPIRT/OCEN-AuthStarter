package org.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerReactiveAuthenticationManagerResolver;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final String CONSENT_SECURITY_POLICY = "default-src 'none';";

    @Bean
    protected SecurityWebFilterChain apiSecurity(ServerHttpSecurity http,
                                                 @Value("${api.security.jwt.issuer}") String apiTokenIssuer) {
        final var authManagerResolver = new JwtIssuerReactiveAuthenticationManagerResolver(apiTokenIssuer);
        http.csrf().disable()
                .httpBasic().disable()
                .cors().and()
                .headers(headers ->
                        headers
                                .contentSecurityPolicy(contentSecurityPolicy ->
                                        contentSecurityPolicy
                                                .policyDirectives(CONSENT_SECURITY_POLICY)
                                )
                )
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**", "/common/heartbeat/**", "/health/**").permitAll()
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.authenticationManagerResolver(authManagerResolver));
        return http.build();
    }
}

