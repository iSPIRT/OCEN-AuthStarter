package com.example.ocenauthentication.filter.ackresponse;

import com.example.ocenauthentication.jws.JWSSigner;
import com.example.ocenauthentication.util.ReactiveResponseSigner;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

@Component
// This Filter attaches the Signature in the Acknowledgement Response using Lender KeySet
public class JwsAckResponseWebFilter implements WebFilter {

    protected final JWSSigner signer;

    public JwsAckResponseWebFilter(final JWSSigner signer) {
        this.signer = signer;
    }

    @Override
    @Nonnull
    public Mono<Void> filter(@Nonnull ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(new ExchangeDecorator(exchange, signer));
    }

    private static class ExchangeDecorator extends ServerWebExchangeDecorator {

        private final JWSSigner signer;

        protected ExchangeDecorator(ServerWebExchange delegate, JWSSigner signer) {
            super(delegate);
            this.signer = signer;
        }

        @Override
        @Nonnull
        public ServerHttpResponse getResponse() {
            return new ReactiveResponseSigner(super.getResponse(), signer);
        }
    }
}
