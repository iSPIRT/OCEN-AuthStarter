package org.example.dto.registry;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductNetworkDetail {
    @JsonProperty("LOAN_AGENT")
    private List<ParticipantDetail> loanAgents;

    @JsonProperty("LENDER")
    private List<ParticipantDetail> lenders;
}
