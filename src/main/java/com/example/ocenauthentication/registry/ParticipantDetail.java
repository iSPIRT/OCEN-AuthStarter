package com.example.ocenauthentication.registry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantDetail {
    private String id;
    private ParticipantRole participantRole;
    private String createdOn;
    private String updatedOn;
    private String kcClientId;
    private String kcClientSecret;
    private String publicKey;
    private String approvedBy;
    private boolean isApproved;
    private String approvedOn;
    private String participantRoleStatus;
    private Participant participant;
}
