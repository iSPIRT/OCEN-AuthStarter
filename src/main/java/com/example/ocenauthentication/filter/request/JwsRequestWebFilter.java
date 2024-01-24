/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package com.example.ocenauthentication.filter.request;

import com.example.ocenauthentication.filter.ackresponse.JwsAckResponseWebFilter;
import com.example.ocenauthentication.jws.JWSSigner;
import com.example.ocenauthentication.registry.RegistryService;
import lombok.extern.log4j.Log4j2;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.lang.JoseException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
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

import static com.example.ocenauthentication.jws.JWSResponseValidator.parseSign;


@Log4j2
// This request filter verifies the signature using the LA public key
public class JwsRequestWebFilter extends JwsAckResponseWebFilter {

    public static final String PARTICIPANT_ID = "1802";
    protected final List<PathPattern> pathPatterns;
    private final RegistryService registryService;

    public JwsRequestWebFilter(JWSSigner signer,
                               List<PathPattern> pathPatterns,
                               RegistryService registryService) {
        super(signer);
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
                return super.filter(exchange, chain);
            }
        }

        if (pathContainer.value().equalsIgnoreCase("/Heartbeat"))
            return super.filter(exchange, chain);

        if (pathContainer.value().equalsIgnoreCase("/generate-signature"))
            return super.filter(exchange, chain);


        ServerHttpRequestDecorator requestDecorator = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            @Nonnull
            public Flux<DataBuffer> getBody() {
                Mono<String> laPublicKey = registryService.getEntity(PARTICIPANT_ID)
                        .map(participantDetail -> participantDetail.getPublicKey());

                return Mono.zip(DataBufferUtils.join(super.getBody()), laPublicKey,
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
        return super.filter(exchange.mutate().request(requestDecorator).build(), chain);
    }
}
