package org.example.registry;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ParticipantRole getParticipantRole() {
        return participantRole;
    }

    public void setParticipantRole(ParticipantRole participantRole) {
        this.participantRole = participantRole;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(String updatedOn) {
        this.updatedOn = updatedOn;
    }

    public String getKcClientId() {
        return kcClientId;
    }

    public void setKcClientId(String kcClientId) {
        this.kcClientId = kcClientId;
    }

    public String getKcClientSecret() {
        return kcClientSecret;
    }

    public void setKcClientSecret(String kcClientSecret) {
        this.kcClientSecret = kcClientSecret;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public String getApprovedOn() {
        return approvedOn;
    }

    public void setApprovedOn(String approvedOn) {
        this.approvedOn = approvedOn;
    }

    public String getParticipantRoleStatus() {
        return participantRoleStatus;
    }

    public void setParticipantRoleStatus(String participantRoleStatus) {
        this.participantRoleStatus = participantRoleStatus;
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }
}
