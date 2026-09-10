package com.loanunderwriting.application.dto;

import com.loanunderwriting.application.model.ApplicationStatus;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplicationResponseDTO {

    private String id;
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
    private ApplicationStatus status;
    private String decisionReason;
    private String createdAt;
    private String message;
}