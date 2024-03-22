package org.example.jws;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JWSResponseValidator {

    private static final JoseException exception = new JoseException("Signature verification failed");

    public static void parseSign(String detachedSignature, byte[] payload, JsonWebKey jsonWebKey) throws JoseException {
        doParseSign(detachedSignature, Payload.of(payload), jsonWebKey);
    }

    public static void parseSign(String detachedSignature, String payload, JsonWebKey jsonWebKey) throws JoseException {
        doParseSign(detachedSignature, Payload.of(payload), jsonWebKey);
    }

    public static <T> void doParseSign(String detachedSignature, Payload<T> payload, JsonWebKey jsonWebKey) throws JoseException {

        // Use a JsonWebSignature object to verify the signature
        JsonWebSignature verifierJws = new JsonWebSignature();

        // Set the algorithm constraints based on what is agreed upon or expected from
        // the sender
        verifierJws.setAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,
                AlgorithmIdentifiers.RSA_USING_SHA256));

        verifierJws.setCompactSerialization(detachedSignature);
        if (payload != null) {
            // The unencoded detached content is the payload
//            verifierJws.setEncodedPayload(null); //https://stackoverflow.com/questions/70380691/verifying-jws-with-detached-payload-using-jose4j-fails
            payload.apply(verifierJws);
        }

        RsaJsonWebKey jwk = (RsaJsonWebKey) jsonWebKey;

        // The public key is used to verify the signature
        // This should be the public key of the sender.
        verifierJws.setKey(jwk.getPublicKey());

        if (!verifierJws.verifySignature()) {
            throw exception;
        }
    }
}
