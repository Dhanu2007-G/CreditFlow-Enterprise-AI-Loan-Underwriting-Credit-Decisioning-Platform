package com.loanunderwriting.underwriting.service;

import com.loanunderwriting.underwriting.client.ApplicationServiceClient;
import com.loanunderwriting.underwriting.dto.LoanApplicationEventDTO;
import com.loanunderwriting.underwriting.dto.RiskAssessmentDTO;
import com.loanunderwriting.underwriting.dto.UnderwritingDecisionResponseDTO;
import com.loanunderwriting.underwriting.model.DecisionOutcome;
import com.loanunderwriting.underwriting.model.RiskLevel;
import com.loanunderwriting.underwriting.model.UnderwritingDecision;
import com.loanunderwriting.underwriting.repository.UnderwritingDecisionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnderwritingService {

    private final UnderwritingDecisionRepository decisionRepository;
    private final RiskScoringEngine riskScoringEngine;
    private final ApplicationServiceClient applicationServiceClient;

    @Transactional
    public void processLoanApplication(LoanApplicationEventDTO event) {
        log.info("Processing loan application: {}", event.getApplicationId());

        RiskAssessmentDTO assessment = riskScoringEngine.assess(event);

        DecisionOutcome outcome = determineOutcome(assessment);
        BigDecimal approvedAmount = calculateApprovedAmount(event, assessment);
        BigDecimal interestRate = calculateInterestRate(assessment);

        UnderwritingDecision decision = UnderwritingDecision.builder()
                .applicationId(event.getApplicationId())
                .applicantName(event.getApplicantName())
                .email(event.getEmail())
                .loanAmount(event.getLoanAmount())
                .creditScore(event.getCreditScore())
                .annualIncome(event.getAnnualIncome())
                .monthlyDebt(event.getMonthlyDebt())
                .debtToIncomeRatio(assessment.getDebtToIncomeRatio())
                .riskScore(assessment.getRiskScore())
                .riskLevel(assessment.getRiskLevel())
                .outcome(outcome)
                .decisionReasons(String.join("; ", assessment.getRiskReasons()))
                .aiExplanation("Rule-based analysis: " +
                        assessment.getRiskReasons().size() +
                        " risk factor(s) identified")
                .approvedAmount(approvedAmount)
                .suggestedInterestRate(interestRate)
                .build();

        UnderwritingDecision saved = decisionRepository.save(decision);
        log.info("Underwriting decision saved: {} | outcome: {}",
                saved.getId(), saved.getOutcome());

        // Update application status
        String status = mapOutcomeToStatus(outcome);
        String reason = assessment.getRiskReasons().isEmpty()
                ? "All criteria met"
                : String.join("; ", assessment.getRiskReasons());

        applicationServiceClient.updateApplicationStatus(
                event.getApplicationId(), status, reason);
    }

    public UnderwritingDecisionResponseDTO getDecisionByApplicationId(
            String applicationId) {
        UnderwritingDecision decision = decisionRepository
                .findByApplicationId(applicationId)
                .orElseThrow(() -> new RuntimeException(
                        "Decision not found for application: " + applicationId));
        return mapToResponseDTO(decision);
    }

    public List<UnderwritingDecisionResponseDTO> getAllDecisions() {
        return decisionRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<UnderwritingDecisionResponseDTO> getDecisionsByRiskLevel(
            RiskLevel riskLevel) {
        return decisionRepository.findByRiskLevel(riskLevel)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<UnderwritingDecisionResponseDTO> getDecisionsByOutcome(
            DecisionOutcome outcome) {
        return decisionRepository.findByOutcome(outcome)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private DecisionOutcome determineOutcome(RiskAssessmentDTO assessment) {
        if (assessment.getRiskLevel() == RiskLevel.CRITICAL) {
            return DecisionOutcome.REJECTED;
        } else if (assessment.getRiskLevel() == RiskLevel.HIGH) {
            return DecisionOutcome.CONDITIONAL_APPROVAL;
        } else if (assessment.getRiskLevel() == RiskLevel.MEDIUM) {
            return DecisionOutcome.CONDITIONAL_APPROVAL;
        } else {
            return DecisionOutcome.APPROVED;
        }
    }

    private BigDecimal calculateApprovedAmount(
            LoanApplicationEventDTO event, RiskAssessmentDTO assessment) {
        if (assessment.getRiskLevel() == RiskLevel.CRITICAL) {
            return BigDecimal.ZERO;
        } else if (assessment.getRiskLevel() == RiskLevel.HIGH) {
            return event.getLoanAmount().multiply(BigDecimal.valueOf(0.5));
        } else if (assessment.getRiskLevel() == RiskLevel.MEDIUM) {
            return event.getLoanAmount().multiply(BigDecimal.valueOf(0.75));
        }
        return event.getLoanAmount();
    }

    private BigDecimal calculateInterestRate(RiskAssessmentDTO assessment) {
        if (assessment.getRiskLevel() == RiskLevel.CRITICAL) {
            return BigDecimal.ZERO;
        } else if (assessment.getRiskLevel() == RiskLevel.HIGH) {
            return BigDecimal.valueOf(12.99);
        } else if (assessment.getRiskLevel() == RiskLevel.MEDIUM) {
            return BigDecimal.valueOf(8.99);
        }
        return BigDecimal.valueOf(5.99);
    }

    private String mapOutcomeToStatus(DecisionOutcome outcome) {
        return switch (outcome) {
            case APPROVED -> "APPROVED";
            case REJECTED -> "REJECTED";
            case CONDITIONAL_APPROVAL -> "CONDITIONAL_APPROVAL";
            default -> "UNDER_REVIEW";
        };
    }

    private UnderwritingDecisionResponseDTO mapToResponseDTO(
            UnderwritingDecision decision) {
        return UnderwritingDecisionResponseDTO.builder()
                .id(decision.getId())
                .applicationId(decision.getApplicationId())
                .applicantName(decision.getApplicantName())
                .email(decision.getEmail())
                .loanAmount(decision.getLoanAmount())
                .creditScore(decision.getCreditScore())
                .annualIncome(decision.getAnnualIncome())
                .monthlyDebt(decision.getMonthlyDebt())
                .debtToIncomeRatio(decision.getDebtToIncomeRatio())
                .riskScore(decision.getRiskScore())
                .riskLevel(decision.getRiskLevel())
                .outcome(decision.getOutcome())
                .decisionReasons(decision.getDecisionReasons())
                .aiExplanation(decision.getAiExplanation())
                .approvedAmount(decision.getApprovedAmount())
                .suggestedInterestRate(decision.getSuggestedInterestRate())
                .createdAt(decision.getCreatedAt() != null ?
                        decision.getCreatedAt().toString() : null)
                .build();
    }
}