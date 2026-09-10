package com.loanunderwriting.underwriting.kafka;

import com.loanunderwriting.underwriting.dto.LoanApplicationEventDTO;
import com.loanunderwriting.underwriting.service.UnderwritingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationConsumer {

    private final UnderwritingService underwritingService;

    @KafkaListener(
            topics = "${kafka.topics.loan-applications}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeLoanApplicationEvent(
            @Payload LoanApplicationEventDTO event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received loan application event | topic: {} | " +
                        "partition: {} | offset: {} | applicationId: {}",
                topic, partition, offset, event.getApplicationId());

        try {
            underwritingService.processLoanApplication(event);
            log.info("Loan application processed successfully: {}",
                    event.getApplicationId());
        } catch (Exception e) {
            log.error("Error processing loan application: {} | error: {}",
                    event.getApplicationId(), e.getMessage());
        }
    }
}