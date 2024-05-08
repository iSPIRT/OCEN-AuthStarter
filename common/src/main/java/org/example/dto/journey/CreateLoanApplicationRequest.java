package org.example.dto.journey;

import lombok.Data;

import java.util.List;

@Data
public class CreateLoanApplicationRequest {
    private MetaData metadata;
    private ProductData productData;
    private List<LoanApplication> loanApplications;
}