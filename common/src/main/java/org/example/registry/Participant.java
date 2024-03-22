package org.example.registry;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Participant {
    private String id;
    private String participantName;
    private String participantDescription;
    private String participantLogo;
    private String participantLogoContentType;
    private String participantWebsite;
    private String participantEmail;
    private String participantPhone;
    private String createdOn;
    private String updatedOn;
}
