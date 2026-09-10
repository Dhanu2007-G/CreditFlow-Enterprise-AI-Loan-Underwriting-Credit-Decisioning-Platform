package com.loanunderwriting.application.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplicationRequestDTO {

    @NotBlank(message = "Applicant name is required")
    private String applicantName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum loan amount is $1,000")
    @DecimalMax(value = "1000000.00", message = "Maximum loan amount is $1,000,000")
    private BigDecimal loanAmount;

    @NotBlank(message = "Loan purpose is required")
    private String loanPurpose;

    @NotNull(message = "Loan term is required")
    @Min(value = 12, message = "Minimum loan term is 12 months")
    @Max(value = 360, message = "Maximum loan term is 360 months")
    private Integer loanTermMonths;

    @NotNull(message = "Annual income is required")
    @DecimalMin(value = "0.01", message = "Annual income must be positive")
    private BigDecimal annualIncome;

    @NotNull(message = "Credit score is required")
    @Min(value = 300, message = "Minimum credit score is 300")
    @Max(value = 850, message = "Maximum credit score is 850")
    private Integer creditScore;

    @NotNull(message = "Monthly debt is required")
    @DecimalMin(value = "0.00", message = "Monthly debt cannot be negative")
    private BigDecimal monthlyDebt;

    @NotBlank(message = "Employment type is required")
    private String employmentType;

    @NotNull(message = "Employment years is required")
    @Min(value = 0, message = "Employment years cannot be negative")
    private Integer employmentYears;
}