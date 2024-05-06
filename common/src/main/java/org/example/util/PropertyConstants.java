package org.example.util;


public class PropertyConstants {
    public static final String LENDER_CREDENTIALS_FILE = "lender_private_public_keypair_set.json";

    public static final String CREATE_LOAN_APPLICATION_RESPONSE_JSON = "create_loan_application_response.json";

    public static final String LOAN_AGENT_CREDENTIALS_FILE = "loan_agent_private_public_keypair_set.json";

    public static final String CREATE_LOAN_APPLICATION_REQUEST_JSON = "create_loan_application_request.json";

    public static final String CLIENT_ID = "${client.id}";
    public static final String CLIENT_SECRET = "${client.secret}";

    public static final String OCEN_TOKEN_GEN_URL = "${ocen.token.generation.url}";

    public static final String OCEN_REGISTRY_BASE_URL = "${ocen.registry.base.url}";

    public static final String OCEN_API_SECURITY_JWT_ISSUER = "${ocen.api.security.jwt.issuer}";

    public static final String OCEN_HEARTBEAT_EVENT_URL = "${ocen.heartbeat.event.url}";

    public static final String PRODUCT_ID = "${product.id}";

    public static final String PRODUCT_NETWORK_ID = "${product.network.id}";

}
