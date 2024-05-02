package org.example.dto;

import lombok.Data;

@Data
public class CreateLoanApplicationRequest {
    private MetaData metadata;
    private ProductData productData;
    private Object loanApplications;
}
