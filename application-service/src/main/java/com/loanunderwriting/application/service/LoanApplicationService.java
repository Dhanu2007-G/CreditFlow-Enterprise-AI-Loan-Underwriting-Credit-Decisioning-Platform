package com.loanunderwriting.application.service;

import com.loanunderwriting.application.dto.LoanApplicationEventDTO;
import com.loanunderwriting.application.dto.LoanApplicationRequestDTO;
import com.loanunderwriting.application.dto.LoanApplicationResponseDTO;
import com.loanunderwriting.application.kafka.LoanApplicationEventProducer;
import com.loanunderwriting.application.model.ApplicationStatus;
import com.loanunderwriting.application.model.LoanApplication;
import com.loanunderwriting.application.repository.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanApplicationEventProducer eventProducer;

    @Transactional
    public LoanApplicationResponseDTO submitApplication(
            LoanApplicationRequestDTO request) {
        log.info("Submitting loan application for applicant: {}",
                request.getApplicantName());

        LoanApplication application = LoanApplication.builder()
                .applicantId(UUID.randomUUID().toString())
                .applicantName(request.getApplicantName())
                .email(request.getEmail())
                .loanAmount(request.getLoanAmount())
                .loanPurpose(request.getLoanPurpose())
                .loanTermMonths(request.getLoanTermMonths())
                .annualIncome(request.getAnnualIncome())
                .creditScore(request.getCreditScore())
                .monthlyDebt(request.getMonthlyDebt())
                .employmentType(request.getEmploymentType())
                .employmentYears(request.getEmploymentYears())
                .build();

        LoanApplication saved = loanApplicationRepository.save(application);
        log.info("Loan application saved with id: {}", saved.getId());

        LoanApplicationEventDTO event = LoanApplicationEventDTO.builder()
                .applicationId(saved.getId())
                .applicantId(saved.getApplicantId())
                .applicantName(saved.getApplicantName())
                .email(saved.getEmail())
                .loanAmount(saved.getLoanAmount())
                .loanPurpose(saved.getLoanPurpose())
                .loanTermMonths(saved.getLoanTermMonths())
                .annualIncome(saved.getAnnualIncome())
                .creditScore(saved.getCreditScore())
                .monthlyDebt(saved.getMonthlyDebt())
                .employmentType(saved.getEmploymentType())
                .employmentYears(saved.getEmploymentYears())
                .createdAt(saved.getCreatedAt().toString())
                .build();

        eventProducer.publishLoanApplicationEvent(event);

        return mapToResponseDTO(saved,
                "Loan application submitted and sent for underwriting analysis");
    }

    @Cacheable(value = "loan-applications", key = "#applicationId")
    public LoanApplicationResponseDTO getApplicationById(String applicationId) {
        log.info("Fetching loan application by id: {}", applicationId);
        LoanApplication application = loanApplicationRepository
                .findById(applicationId)
                .orElseThrow(() -> new RuntimeException(
                        "Loan application not found: " + applicationId));
        return mapToResponseDTO(application, null);
    }

    public List<LoanApplicationResponseDTO> getApplicationsByEmail(String email) {
        log.info("Fetching loan applications for email: {}", email);
        return loanApplicationRepository.findByEmail(email)
                .stream()
                .map(a -> mapToResponseDTO(a, null))
                .collect(Collectors.toList());
    }

    public List<LoanApplicationResponseDTO> getApplicationsByStatus(
            ApplicationStatus status) {
        log.info("Fetching loan applications by status: {}", status);
        return loanApplicationRepository.findByStatus(status)
                .stream()
                .map(a -> mapToResponseDTO(a, null))
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "loan-applications", key = "#applicationId")
    public LoanApplicationResponseDTO updateApplicationStatus(
            String applicationId, ApplicationStatus status, String reason) {
        log.info("Updating application {} status to {}", applicationId, status);
        LoanApplication application = loanApplicationRepository
                .findById(applicationId)
                .orElseThrow(() -> new RuntimeException(
                        "Loan application not found: " + applicationId));
        application.setStatus(status);
        application.setDecisionReason(reason);
        LoanApplication updated = loanApplicationRepository.save(application);
        return mapToResponseDTO(updated, "Application status updated");
    }

    private LoanApplicationResponseDTO mapToResponseDTO(
            LoanApplication application, String message) {
        return LoanApplicationResponseDTO.builder()
                .id(application.getId())
                .applicantId(application.getApplicantId())
                .applicantName(application.getApplicantName())
                .email(application.getEmail())
                .loanAmount(application.getLoanAmount())
                .loanPurpose(application.getLoanPurpose())
                .loanTermMonths(application.getLoanTermMonths())
                .annualIncome(application.getAnnualIncome())
                .creditScore(application.getCreditScore())
                .monthlyDebt(application.getMonthlyDebt())
                .employmentType(application.getEmploymentType())
                .employmentYears(application.getEmploymentYears())
                .status(application.getStatus())
                .decisionReason(application.getDecisionReason())
                .createdAt(application.getCreatedAt() != null ?
                        application.getCreatedAt().toString() : null)
                .message(message)
                .build();
    }
}