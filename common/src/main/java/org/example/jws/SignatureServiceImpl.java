package org.example.jws;

import org.jose4j.lang.JoseException;
import org.springframework.stereotype.Service;

@Service
public class SignatureServiceImpl implements SignatureService {

    private final JWSSigner applicationSigner;

    public SignatureServiceImpl(JWSSigner signer) {
        this.applicationSigner = signer;
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
            return jwsSigner.sign(body);
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
