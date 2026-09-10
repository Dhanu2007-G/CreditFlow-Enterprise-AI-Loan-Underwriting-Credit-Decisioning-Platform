package com.loanunderwriting.notification.service;

import com.loanunderwriting.notification.dto.LoanApplicationEventDTO;
import com.loanunderwriting.notification.dto.NotificationResponseDTO;
import com.loanunderwriting.notification.model.NotificationRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final DynamoDbService dynamoDbService;
    private final S3Service s3Service;

    public void processLoanApplicationEvent(LoanApplicationEventDTO event) {
        log.info("Processing notification for application: {}",
                event.getApplicationId());

        NotificationRecord record = NotificationRecord.builder()
                .applicationId(event.getApplicationId())
                .applicantName(event.getApplicantName())
                .email(event.getEmail())
                .loanAmount(event.getLoanAmount())
                .loanPurpose(event.getLoanPurpose())
                .status("RECEIVED")
                .message("Loan application received and queued for underwriting")
                .build();

        String notificationId = dynamoDbService.saveNotification(record);
        log.info("Notification saved to DynamoDB: {}", notificationId);

        s3Service.saveAuditLog(record);
        log.info("Audit log saved to S3 for application: {}",
                event.getApplicationId());
    }

    public List<NotificationResponseDTO> getAllNotifications() {
        return dynamoDbService.getAllNotifications()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationResponseDTO> getNotificationsByApplicationId(
            String applicationId) {
        return dynamoDbService.getNotificationsByApplicationId(applicationId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private NotificationResponseDTO mapToDTO(
            Map<String, AttributeValue> item) {
        return NotificationResponseDTO.builder()
                .id(getStr(item, "id"))
                .applicationId(getStr(item, "applicationId"))
                .applicantName(getStr(item, "applicantName"))
                .email(getStr(item, "email"))
                .loanAmount(item.containsKey("loanAmount") ?
                        new BigDecimal(item.get("loanAmount").n()) : null)
                .loanPurpose(getStr(item, "loanPurpose"))
                .status(getStr(item, "status"))
                .message(getStr(item, "message"))
                .createdAt(getStr(item, "createdAt"))
                .build();
    }

    private String getStr(Map<String, AttributeValue> item, String key) {
        return item.containsKey(key) ? item.get(key).s() : null;
    }
}