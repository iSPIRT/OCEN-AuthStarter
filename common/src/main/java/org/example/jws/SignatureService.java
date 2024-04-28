package org.example.jws;

import org.jose4j.jwt.consumer.InvalidJwtException;

public interface SignatureService {

    String generateParticipantSignature(String body);

    String generateSignature(String body, String keyset);

    boolean verifySignature(String body, String signature, String keyset);

    boolean verifyTokenSignature(String bearerToken) throws InvalidJwtException;

    boolean verifyRequesterSignature(String bearerToken, String signature);
}
