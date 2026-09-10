package com.loanunderwriting.underwriting.dto;

import com.loanunderwriting.underwriting.model.DecisionOutcome;
import com.loanunderwriting.underwriting.model.RiskLevel;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnderwritingDecisionResponseDTO {

    private String id;
    private String applicationId;
    private String applicantName;
    private String email;
    private BigDecimal loanAmount;
    private Integer creditScore;
    private BigDecimal annualIncome;
    private BigDecimal monthlyDebt;
    private Double debtToIncomeRatio;
    private Double riskScore;
    private RiskLevel riskLevel;
    private DecisionOutcome outcome;
    private String decisionReasons;
    private String aiExplanation;
    private BigDecimal approvedAmount;
    private BigDecimal suggestedInterestRate;
    private String createdAt;
}