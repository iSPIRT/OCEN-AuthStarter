package org.example.jws;

public interface SignatureService {

    String generateParticipantSignature(String body);

    String generateSignature(String body, String keyset);
}
