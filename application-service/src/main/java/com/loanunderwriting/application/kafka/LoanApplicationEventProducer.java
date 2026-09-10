package com.loanunderwriting.application.kafka;

import com.loanunderwriting.application.dto.LoanApplicationEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationEventProducer {

    private final KafkaTemplate<String, LoanApplicationEventDTO> kafkaTemplate;

    @Value("${kafka.topics.loan-applications}")
    private String loanApplicationsTopic;

    public void publishLoanApplicationEvent(LoanApplicationEventDTO event) {
        log.info("Publishing loan application event for applicationId: {}",
                event.getApplicationId());

        CompletableFuture<SendResult<String, LoanApplicationEventDTO>> future =
                kafkaTemplate.send(loanApplicationsTopic,
                        event.getApplicationId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Loan application event published successfully | " +
                                "applicationId: {} | partition: {} | offset: {}",
                        event.getApplicationId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish loan application event | " +
                                "applicationId: {} | error: {}",
                        event.getApplicationId(), ex.getMessage());
            }
        });
    }
}