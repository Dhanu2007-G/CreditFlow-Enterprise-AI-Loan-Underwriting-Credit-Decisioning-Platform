package com.loanunderwriting.underwriting.dto;

import com.loanunderwriting.underwriting.model.RiskLevel;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskAssessmentDTO {

    private String applicationId;
    private Double riskScore;
    private RiskLevel riskLevel;
    private Double debtToIncomeRatio;
    private List<String> riskReasons;
    private boolean requiresAiAnalysis;
}