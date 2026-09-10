package com.loanunderwriting.application.controller;

import com.loanunderwriting.application.dto.LoanApplicationRequestDTO;
import com.loanunderwriting.application.dto.LoanApplicationResponseDTO;
import com.loanunderwriting.application.model.ApplicationStatus;
import com.loanunderwriting.application.service.LoanApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Loan Application API",
        description = "Endpoints for submitting and managing loan applications")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    @PostMapping
    @Operation(summary = "Submit a new loan application")
    public ResponseEntity<LoanApplicationResponseDTO> submitApplication(
            @Valid @RequestBody LoanApplicationRequestDTO request) {
        log.info("Received loan application from: {}", request.getApplicantName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(loanApplicationService.submitApplication(request));
    }

    @GetMapping("/{applicationId}")
    @Operation(summary = "Get loan application by ID")
    public ResponseEntity<LoanApplicationResponseDTO> getApplicationById(
            @PathVariable String applicationId) {
        return ResponseEntity.ok(
                loanApplicationService.getApplicationById(applicationId));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get loan applications by email")
    public ResponseEntity<List<LoanApplicationResponseDTO>> getApplicationsByEmail(
            @PathVariable String email) {
        return ResponseEntity.ok(
                loanApplicationService.getApplicationsByEmail(email));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get loan applications by status")
    public ResponseEntity<List<LoanApplicationResponseDTO>> getApplicationsByStatus(
            @PathVariable ApplicationStatus status) {
        return ResponseEntity.ok(
                loanApplicationService.getApplicationsByStatus(status));
    }

    @PatchMapping("/{applicationId}/status")
    @Operation(summary = "Update loan application status")
    public ResponseEntity<LoanApplicationResponseDTO> updateApplicationStatus(
            @PathVariable String applicationId,
            @RequestParam ApplicationStatus status,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(
                loanApplicationService.updateApplicationStatus(
                        applicationId, status, reason));
    }
}