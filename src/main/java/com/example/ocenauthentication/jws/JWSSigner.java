package com.example.ocenauthentication.jws;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.VerificationJwkSelector;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.HeaderParameterNames;
import org.jose4j.lang.JoseException;

public class JWSSigner {

    private final JsonWebKeySet jsonWebKeySet;
    private final String publicKeyAsJson;

    public JWSSigner(String jsonWebKeySet) throws JoseException {
        this.jsonWebKeySet = new JsonWebKeySet(jsonWebKeySet);
        publicKeyAsJson = this.jsonWebKeySet.getJsonWebKeys().get(0).toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY);
    }


    /**
     * This method generates a detached json web signature,
     * Using the RFC 7797 JWS Unencoded Payload Option
     *
     * @param payload to sign (as string)
     * @return signature without the payload (i.e. detached signature)
     * @throws JoseException
     */
    public String sign(String payload) throws JoseException {
        return doSign(Payload.of(payload), true);
    }

    /**
     * This method generates a detached json web signature,
     * Using the RFC 7797 JWS Unencoded Payload Option
     *
     * @param payload to sign (as byte[])
     * @return signature without the payload (i.e. detached signature)
     * @throws JoseException
     */
    public String sign(byte[] payload) throws JoseException {
        return doSign(Payload.of(payload), true);
    }

    public String signEmbedded(String payload) throws JoseException {
        return doSign(Payload.of(payload), false);
    }

    public String signEmbedded(byte[] payload) throws JoseException {
        return doSign(Payload.of(payload), false);
    }

    private <T> String doSign(Payload<T> payload, boolean detached) throws JoseException {
        // Create a new JsonWebSignature object for the signing
        JsonWebSignature signerJws = new JsonWebSignature();

        // The content is the payload of the JWS
        payload.apply(signerJws);

        // Set the signature algorithm on the JWS
        signerJws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);

        RsaJsonWebKey jwk = getJsonWebKey();

        // The private key is used to sign
        signerJws.setKey(jwk.getPrivateKey());

        // Set the Key ID (kid) header because it's just the polite thing to do.
        signerJws.setKeyIdHeaderValue(jwk.getKeyId());

        // Set the "b64" header to false, which indicates that the payload is not
        // encoded
        // when calculating the signature (per RFC 7797)
        signerJws.getHeaders().setObjectHeaderValue(HeaderParameterNames.BASE64URL_ENCODE_PAYLOAD, !detached);

        // Produce the compact serialization with an empty/detached payload,
        // which is the encoded header + ".." + the encoded signature
        if (detached) {
            return signerJws.getDetachedContentCompactSerialization();
        } else {
            // RFC 7797 requires that the "b64" header be listed as critical
            signerJws.setCriticalHeaderNames(HeaderParameterNames.BASE64URL_ENCODE_PAYLOAD);
            return signerJws.getCompactSerialization();
        }
    }

    /**
     * This method validates the detached json web signature with the supplied payload.
     *
     * @param detachedSignature to parse
     * @param payload           as string
     * @throws JoseException - if signature validation fails.
     */
    public JsonWebSignature parseSign(String detachedSignature, String payload) throws JoseException {
        return doParseSign(detachedSignature, Payload.of(payload));
    }

    /**
     * This method validates the detached json web signature with the supplied payload.
     *
     * @param detachedSignature to parse
     * @param payload           as bytes
     * @throws JoseException - if signature validation fails.
     */
    public JsonWebSignature parseSign(String detachedSignature, byte[] payload) throws JoseException {
        return doParseSign(detachedSignature, Payload.of(payload));
    }

    private <T> JsonWebSignature doParseSign(String detachedSignature, Payload<T> payload) throws JoseException {

        // Use a JsonWebSignature object to verify the signature
        JsonWebSignature verifierJws = new JsonWebSignature();

        // Set the algorithm constraints based on what is agreed upon or expected from
        // the sender
        verifierJws.setAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,
                AlgorithmIdentifiers.RSA_USING_SHA256));

        if (payload == null) {
            // The JWS with embedded content is the compact serialization
            verifierJws.setCompactSerialization(detachedSignature);
        } else {
            // The JWS with detached content is the compact serialization
            verifierJws.setCompactSerialization(detachedSignature);

            // The unencoded detached content is the payload
            // verifierJws.setPayload(payload)
            payload.apply(verifierJws);
        }

        VerificationJwkSelector jwkSelector = new VerificationJwkSelector();
        RsaJsonWebKey jwk = (RsaJsonWebKey) jwkSelector.select(verifierJws, jsonWebKeySet.getJsonWebKeys());

        // The public key is used to verify the signature
        // This should be the public key of the sender.
        verifierJws.setKey(jwk.getPublicKey());

        if (!verifierJws.verifySignature()) {
            throw new JoseException("Signature verification failed.");
        }

        // return the jws
        return verifierJws;
    }

    private RsaJsonWebKey getJsonWebKey() {
        return (RsaJsonWebKey) jsonWebKeySet.getJsonWebKeys().get(0);
    }

    public String getPublicKeyAsJson() {
        return publicKeyAsJson;
    }
}
