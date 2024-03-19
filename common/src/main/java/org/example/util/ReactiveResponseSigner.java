package org.example.util;

import lombok.SneakyThrows;
import org.example.jws.JWSSigner;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;

public class ReactiveResponseSigner extends ServerHttpResponseDecorator {

    private final JWSSigner signer;

    @SneakyThrows
    public ReactiveResponseSigner(ServerHttpResponse response, JWSSigner signer) {
        super(response);
        this.signer = signer;
    }


    @Override
    @Nonnull
    public Mono<Void> writeWith(@Nonnull Publisher<? extends DataBuffer> body) {
        Flux<DataBuffer> buffer = Flux.from(body);
        return super.writeWith(buffer.doOnNext(dataBuffer -> {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try {
                Channels.newChannel(stream).write(dataBuffer.asByteBuffer().asReadOnlyBuffer());
                String sign = signer.sign(stream.toByteArray());
                getHeaders().set("x-jws-signature", sign);
            } catch (Exception e) { // IOException merged
                e.printStackTrace();
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
    }
}
