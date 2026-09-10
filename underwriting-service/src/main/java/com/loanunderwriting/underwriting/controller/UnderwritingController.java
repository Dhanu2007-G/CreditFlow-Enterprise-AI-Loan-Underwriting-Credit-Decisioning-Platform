package com.loanunderwriting.underwriting.controller;

import com.loanunderwriting.underwriting.dto.UnderwritingDecisionResponseDTO;
import com.loanunderwriting.underwriting.model.DecisionOutcome;
import com.loanunderwriting.underwriting.model.RiskLevel;
import com.loanunderwriting.underwriting.service.UnderwritingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/underwriting")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Underwriting API",
        description = "Endpoints for underwriting decisions and risk assessments")
public class UnderwritingController {

    private final UnderwritingService underwritingService;

    @GetMapping("/decisions")
    @Operation(summary = "Get all underwriting decisions")
    public ResponseEntity<List<UnderwritingDecisionResponseDTO>> getAllDecisions() {
        return ResponseEntity.ok(underwritingService.getAllDecisions());
    }

    @GetMapping("/decisions/application/{applicationId}")
    @Operation(summary = "Get decision by application ID")
    public ResponseEntity<UnderwritingDecisionResponseDTO> getDecisionByApplicationId(
            @PathVariable String applicationId) {
        return ResponseEntity.ok(
                underwritingService.getDecisionByApplicationId(applicationId));
    }

    @GetMapping("/decisions/risk/{riskLevel}")
    @Operation(summary = "Get decisions by risk level")
    public ResponseEntity<List<UnderwritingDecisionResponseDTO>> getDecisionsByRiskLevel(
            @PathVariable RiskLevel riskLevel) {
        return ResponseEntity.ok(
                underwritingService.getDecisionsByRiskLevel(riskLevel));
    }

    @GetMapping("/decisions/outcome/{outcome}")
    @Operation(summary = "Get decisions by outcome")
    public ResponseEntity<List<UnderwritingDecisionResponseDTO>> getDecisionsByOutcome(
            @PathVariable DecisionOutcome outcome) {
        return ResponseEntity.ok(
                underwritingService.getDecisionsByOutcome(outcome));
    }
}