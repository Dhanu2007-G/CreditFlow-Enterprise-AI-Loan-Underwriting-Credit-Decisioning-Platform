package com.loanunderwriting.application.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplicationEventDTO {

    private String applicationId;
    private String applicantId;
    private String applicantName;
    private String email;
    private BigDecimal loanAmount;
    private String loanPurpose;
    private Integer loanTermMonths;
    private BigDecimal annualIncome;
    private Integer creditScore;
    private BigDecimal monthlyDebt;
    private String employmentType;
    private Integer employmentYears;
    private String createdAt;
}