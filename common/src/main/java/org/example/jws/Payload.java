package org.example.jws;

import org.jose4j.jws.JsonWebSignature;

@FunctionalInterface
interface Payload<T> {
    void apply(JsonWebSignature signature);

    static Payload<String> of(String payload) {
        return new StringPayload(payload);
    }

    static Payload<byte[]> of(byte[] payload) {
        return new BytesPayload(payload);
    }

    class StringPayload implements Payload<String> {
        private final String payload;

        StringPayload(String payload) {
            this.payload = payload;
        }

        @Override
        public void apply(JsonWebSignature signature) {
            signature.setPayload(payload);
        }
    }

    class BytesPayload implements Payload<byte[]> {
        private final byte[] payload;

        public BytesPayload(byte[] payload) {
            this.payload = payload;
        }

        @Override
        public void apply(JsonWebSignature signature) {
            signature.setPayloadBytes(payload);
        }
    }
}
