package org.example.dto.journey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateLoanApplicationResponse {
    private MetaData metadata;
    private ProductData productData;
    private Response response;
    private List<LoanApplication> loanApplications;
}
