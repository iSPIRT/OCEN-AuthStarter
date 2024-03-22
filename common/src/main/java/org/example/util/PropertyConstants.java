package org.example.util;


public class PropertyConstants {
    public static final String LENDER_CREDENTIALS_FILE = "lender_private_public_keypair_set.json";
    public static final String LOAN_AGENT_CREDENTIALS_FILE = "loan_agent_private_public_keypair_set.json";

    public static final String KEY_SET_CREDENTIALS_FILE = "${key.set.credentials.file}";

    public static final String PARTICIPANT_ID = "${participant.id}";

    public static final String LOAN_AGENT_PARTICIPANT_ID = "${loan.agent.participant.id}";
    public static final String LENDER_PARTICIPANT_ID = "${lender.participant.id}";

    public static final String CLIENT_ID = "${client.id}";
    public static final String CLIENT_SECRET = "${client.secret}";

    public static final String OCEN_TOKEN_GEN_URL = "${ocen.token.generation.url}";
    public static final String OCEN_REGISTRY_PARTICIPANT_ROLE_URL = "${ocen.participant.roles.url}";
}
