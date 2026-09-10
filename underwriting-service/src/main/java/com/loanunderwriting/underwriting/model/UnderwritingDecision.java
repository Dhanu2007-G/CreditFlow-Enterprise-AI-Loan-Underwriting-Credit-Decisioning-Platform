package com.loanunderwriting.underwriting.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "underwriting_decisions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnderwritingDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String applicationId;

    @Column(nullable = false)
    private String applicantName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private BigDecimal loanAmount;

    @Column(nullable = false)
    private Integer creditScore;

    @Column(nullable = false)
    private BigDecimal annualIncome;

    @Column(nullable = false)
    private BigDecimal monthlyDebt;

    @Column(nullable = false)
    private Double debtToIncomeRatio;

    @Column(nullable = false)
    private Double riskScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DecisionOutcome outcome;

    @Column(length = 2000)
    private String decisionReasons;

    @Column(length = 2000)
    private String aiExplanation;

    @Column
    private BigDecimal approvedAmount;

    @Column
    private BigDecimal suggestedInterestRate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}