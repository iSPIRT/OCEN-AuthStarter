/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package org.example.filter;

import lombok.extern.log4j.Log4j2;
import org.example.registry.RegistryService;
import org.example.util.HeaderConstants;
import org.example.util.PayloadUtil;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.lang.JoseException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.pattern.PathPattern;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.nio.channels.Channels;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.example.jws.JWSResponseValidator.parseSign;


@Log4j2
// This request filter verifies the signature using the participant public key
public class JwsRequestWebFilter implements WebFilter {

    protected final List<PathPattern> pathPatterns;
    private final RegistryService registryService;

    public JwsRequestWebFilter(List<PathPattern> pathPatterns, RegistryService registryService) {
        this.pathPatterns = pathPatterns;
        this.registryService = registryService;
    }

    @Nonnull
    @Override
    public Mono<Void> filter(@Nonnull ServerWebExchange exchange, WebFilterChain chain) {
        final String jwsHeader = exchange.getRequest().getHeaders().getFirst("x-jws-signature");

        PathContainer pathContainer = exchange.getRequest().getPath().pathWithinApplication();
        for (PathPattern pattern : pathPatterns) {
            if (pattern.matches(pathContainer)) {
                return chain.filter(exchange);
            }
        }

        if (pathContainer.value().startsWith("/loan-agent/trigger/**"))
            return chain.filter(exchange);

        if (pathContainer.value().equalsIgnoreCase("/common/heartbeat"))
            return chain.filter(exchange);

        if (pathContainer.value().equalsIgnoreCase("/common/generate-signature"))
            return chain.filter(exchange);


        ServerHttpRequestDecorator requestDecorator = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            @Nonnull
            public Flux<DataBuffer> getBody() {
                String bearerToken = this.getHeaders().getFirst(HeaderConstants.AUTHORIZATION);

                String participantId = PayloadUtil.getParticipantIdFromToken(bearerToken);
                Mono<String> publicKey = registryService.getParticipantDetailByParticipantId(participantId)
                        .map(participantDetail -> participantDetail.getPublicKey());

                return Mono.zip(DataBufferUtils.join(super.getBody()), publicKey,
                                (db, certificate) -> {
                                    try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                                        Channels.newChannel(stream).write(db.asByteBuffer().asReadOnlyBuffer());
                                        certificate = certificate.replaceAll("[\\s\\n]", "");
                                        byte[] bodyAsBytes = stream.toByteArray();
                                        parseSign(jwsHeader, bodyAsBytes, JsonWebKey.Factory.newJwk(certificate));
                                    } catch (JoseException e) {
                                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Signature");
                                    } catch (Exception e) {
                                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request");
                                    }
                                    return Collections.singletonList(db);
                                })
                        .flatMapIterable(Function.identity());
            }

        };
        return chain.filter(exchange.mutate().request(requestDecorator).build());
    }
}
