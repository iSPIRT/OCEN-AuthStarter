package org.example.jws;

import org.example.util.JsonUtil;
import org.example.util.PropertyConstants;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Base64;
import java.util.Map;

@Service
public class SignatureServiceImpl implements SignatureService {

    private final JWSSigner applicationSigner;
    private final JWSSigner tokenSigner;

    public SignatureServiceImpl(JWSSigner signer, @Value(PropertyConstants.OCEN_TOKEN_CERTS_URL) String tokenCertsUrl) throws JoseException {
        this.applicationSigner = signer;

        String jsonWebKeySet = WebClient.builder().build().get().uri(tokenCertsUrl).retrieve().bodyToMono(String.class).block(Duration.ofSeconds(5));
        this.tokenSigner = new JWSSigner(jsonWebKeySet);
    }

    @Override
    public String generateParticipantSignature(String body) {
        try {
            return applicationSigner.sign(body);
        } catch (JoseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String generateSignature(String body, String keyset) {
        try {
            JWSSigner jwsSigner = new JWSSigner(keyset);
            String signature = jwsSigner.sign(body);
            return signature;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public boolean verifyTokenSignature(String bearerToken) {
        String token = bearerToken.replace("Bearer ", "");
        String[] chunks = token.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String payload = new String(decoder.decode(chunks[1]));
        try {
            tokenSigner.parseSign(token, payload);
        } catch (JoseException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean verifyRequesterSignature(String bearerToken, String signature) {
        String token = bearerToken.replace("Bearer", "");
        String[] chunks = token.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String payload = new String(decoder.decode(chunks[1]));
        Map<String, Object> map = JsonUtil.fromJson(payload);
        return true;
    }
}
