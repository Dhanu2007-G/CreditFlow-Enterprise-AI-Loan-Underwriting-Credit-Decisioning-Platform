package com.loanunderwriting.application.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String applicantId;

    @Column(nullable = false)
    private String applicantName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private BigDecimal loanAmount;

    @Column(nullable = false)
    private String loanPurpose;

    @Column(nullable = false)
    private Integer loanTermMonths;

    @Column(nullable = false)
    private BigDecimal annualIncome;

    @Column(nullable = false)
    private Integer creditScore;

    @Column(nullable = false)
    private BigDecimal monthlyDebt;

    @Column(nullable = false)
    private String employmentType;

    @Column(nullable = false)
    private Integer employmentYears;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column
    private String decisionReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = ApplicationStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}