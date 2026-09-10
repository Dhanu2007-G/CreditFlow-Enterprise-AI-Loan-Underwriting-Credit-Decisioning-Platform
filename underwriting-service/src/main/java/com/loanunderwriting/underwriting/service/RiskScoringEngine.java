package com.loanunderwriting.underwriting.service;

import com.loanunderwriting.underwriting.dto.LoanApplicationEventDTO;
import com.loanunderwriting.underwriting.dto.RiskAssessmentDTO;
import com.loanunderwriting.underwriting.model.RiskLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class RiskScoringEngine {

    @Value("${underwriting.rules.min-credit-score}")
    private int minCreditScore;

    @Value("${underwriting.rules.max-debt-to-income-ratio}")
    private double maxDtiRatio;

    @Value("${underwriting.rules.max-loan-amount}")
    private double maxLoanAmount;

    @Value("${underwriting.rules.min-annual-income}")
    private double minAnnualIncome;

    public RiskAssessmentDTO assess(LoanApplicationEventDTO event) {
        log.info("Assessing risk for application: {}", event.getApplicationId());

        List<String> riskReasons = new ArrayList<>();
        double riskScore = 0.0;

        // Rule 1 — Credit score check
        if (event.getCreditScore() < minCreditScore) {
            riskScore += 35.0;
            riskReasons.add("Credit score " + event.getCreditScore() +
                    " is below minimum required score of " + minCreditScore);
        } else if (event.getCreditScore() < 650) {
            riskScore += 15.0;
            riskReasons.add("Credit score " + event.getCreditScore() +
                    " is below preferred score of 650");
        }

        // Rule 2 — Debt to income ratio
        double monthlyIncome = event.getAnnualIncome()
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP)
                .doubleValue();
        double dtiRatio = event.getMonthlyDebt().doubleValue() / monthlyIncome;
        double dtiRatioRounded = Math.round(dtiRatio * 100.0) / 100.0;

        if (dtiRatio > maxDtiRatio) {
            riskScore += 30.0;
            riskReasons.add("Debt-to-income ratio " +
                    String.format("%.1f%%", dtiRatio * 100) +
                    " exceeds maximum of " +
                    String.format("%.0f%%", maxDtiRatio * 100));
        } else if (dtiRatio > 0.36) {
            riskScore += 10.0;
            riskReasons.add("Debt-to-income ratio " +
                    String.format("%.1f%%", dtiRatio * 100) +
                    " is above preferred 36%");
        }

        // Rule 3 — Loan amount vs income
        double loanToIncomeRatio = event.getLoanAmount().doubleValue() /
                event.getAnnualIncome().doubleValue();
        if (loanToIncomeRatio > 5.0) {
            riskScore += 25.0;
            riskReasons.add("Loan amount is " +
                    String.format("%.1f", loanToIncomeRatio) +
                    "x annual income — exceeds 5x threshold");
        } else if (loanToIncomeRatio > 3.0) {
            riskScore += 10.0;
            riskReasons.add("Loan amount is " +
                    String.format("%.1f", loanToIncomeRatio) +
                    "x annual income — above preferred 3x");
        }

        // Rule 4 — Employment check
        if ("SELF_EMPLOYED".equals(event.getEmploymentType()) &&
                event.getEmploymentYears() < 2) {
            riskScore += 20.0;
            riskReasons.add("Self-employed with less than 2 years history");
        } else if (event.getEmploymentYears() < 1) {
            riskScore += 15.0;
            riskReasons.add("Less than 1 year of employment history");
        }

        // Rule 5 — Annual income check
        if (event.getAnnualIncome().doubleValue() < minAnnualIncome) {
            riskScore += 20.0;
            riskReasons.add("Annual income $" +
                    event.getAnnualIncome() +
                    " is below minimum required $" + minAnnualIncome);
        }

        // Cap at 100
        riskScore = Math.min(riskScore, 100.0);
        RiskLevel riskLevel = determineRiskLevel(riskScore);
        boolean requiresAiAnalysis = riskScore >= 30.0;

        log.info("Risk assessment complete for application: {} | " +
                        "score: {} | level: {} | DTI: {}",
                event.getApplicationId(), riskScore, riskLevel, dtiRatioRounded);

        return RiskAssessmentDTO.builder()
                .applicationId(event.getApplicationId())
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .debtToIncomeRatio(dtiRatioRounded)
                .riskReasons(riskReasons)
                .requiresAiAnalysis(requiresAiAnalysis)
                .build();
    }

    private RiskLevel determineRiskLevel(double score) {
        if (score >= 70.0) return RiskLevel.CRITICAL;
        if (score >= 40.0) return RiskLevel.HIGH;
        if (score >= 20.0) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }
}