package com.loanunderwriting.underwriting.repository;

import com.loanunderwriting.underwriting.model.DecisionOutcome;
import com.loanunderwriting.underwriting.model.RiskLevel;
import com.loanunderwriting.underwriting.model.UnderwritingDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnderwritingDecisionRepository
        extends JpaRepository<UnderwritingDecision, String> {

    Optional<UnderwritingDecision> findByApplicationId(String applicationId);

    List<UnderwritingDecision> findByRiskLevel(RiskLevel riskLevel);

    List<UnderwritingDecision> findByOutcome(DecisionOutcome outcome);

    List<UnderwritingDecision> findByEmail(String email);
}