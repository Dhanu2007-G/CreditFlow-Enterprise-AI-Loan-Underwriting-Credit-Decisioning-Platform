package com.loanunderwriting.notification.kafka;

import com.loanunderwriting.notification.dto.LoanApplicationEventDTO;
import com.loanunderwriting.notification.service.NotificationService;
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

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "${kafka.topics.loan-applications}",
            groupId = "loan-notification-group",
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
            notificationService.processLoanApplicationEvent(event);
            log.info("Notification processed for application: {}",
                    event.getApplicationId());
        } catch (Exception e) {
            log.error("Error processing notification for application: {} | error: {}",
                    event.getApplicationId(), e.getMessage());
        }
    }
}