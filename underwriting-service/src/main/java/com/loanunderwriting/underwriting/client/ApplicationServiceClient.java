package com.loanunderwriting.underwriting.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.application-service.url}")
    private String applicationServiceUrl;

    public void updateApplicationStatus(String applicationId,
                                        String status,
                                        String reason) {
        log.info("Updating application {} status to {}", applicationId, status);
        try {
            webClientBuilder.build()
                    .patch()
                    .uri(applicationServiceUrl +
                                    "/api/v1/loans/{id}/status?status={status}&reason={reason}",
                            applicationId, status, reason)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response ->
                            log.info("Application {} status updated to {}",
                                    applicationId, status))
                    .doOnError(error ->
                            log.error("Failed to update application {} status: {}",
                                    applicationId, error.getMessage()))
                    .subscribe();
        } catch (Exception e) {
            log.error("Error calling application service: {}", e.getMessage());
        }
    }
}