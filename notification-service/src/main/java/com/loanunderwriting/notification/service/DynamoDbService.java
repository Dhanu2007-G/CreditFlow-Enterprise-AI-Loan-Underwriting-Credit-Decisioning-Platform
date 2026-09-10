package com.loanunderwriting.notification.service;

import com.loanunderwriting.notification.model.NotificationRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DynamoDbService {

    private final DynamoDbClient dynamoDbClient;

    @Value("${aws.dynamodb.table-name}")
    private String tableName;

    public String saveNotification(NotificationRecord record) {
        String id = UUID.randomUUID().toString();
        log.info("Saving notification to DynamoDB for applicationId: {}",
                record.getApplicationId());

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().s(id).build());
        item.put("applicationId", AttributeValue.builder()
                .s(record.getApplicationId()).build());
        item.put("applicantName", AttributeValue.builder()
                .s(record.getApplicantName()).build());
        item.put("email", AttributeValue.builder()
                .s(record.getEmail()).build());
        item.put("loanAmount", AttributeValue.builder()
                .n(record.getLoanAmount().toString()).build());
        item.put("loanPurpose", AttributeValue.builder()
                .s(record.getLoanPurpose() != null ?
                        record.getLoanPurpose() : "N/A").build());
        item.put("status", AttributeValue.builder()
                .s(record.getStatus() != null ?
                        record.getStatus() : "RECEIVED").build());
        item.put("message", AttributeValue.builder()
                .s(record.getMessage() != null ?
                        record.getMessage() : "").build());
        item.put("createdAt", AttributeValue.builder()
                .s(LocalDateTime.now().toString()).build());

        try {
            dynamoDbClient.putItem(PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .build());
            log.info("Notification saved to DynamoDB with id: {}", id);
            return id;
        } catch (Exception e) {
            log.error("Failed to save notification to DynamoDB: {}",
                    e.getMessage());
            throw new RuntimeException(
                    "Failed to save notification: " + e.getMessage());
        }
    }

    public List<Map<String, AttributeValue>> getNotificationsByApplicationId(
            String applicationId) {
        log.info("Fetching notifications for applicationId: {}", applicationId);
        try {
            ScanResponse response = dynamoDbClient.scan(
                    ScanRequest.builder()
                            .tableName(tableName)
                            .filterExpression("applicationId = :applicationId")
                            .expressionAttributeValues(Map.of(
                                    ":applicationId", AttributeValue.builder()
                                            .s(applicationId).build()))
                            .build());
            return response.items();
        } catch (Exception e) {
            log.error("Failed to fetch notifications: {}", e.getMessage());
            throw new RuntimeException(
                    "Failed to fetch notifications: " + e.getMessage());
        }
    }

    public List<Map<String, AttributeValue>> getAllNotifications() {
        log.info("Fetching all notifications from DynamoDB");
        try {
            ScanResponse response = dynamoDbClient.scan(
                    ScanRequest.builder().tableName(tableName).build());
            return response.items();
        } catch (Exception e) {
            log.error("Failed to fetch notifications: {}", e.getMessage());
            throw new RuntimeException(
                    "Failed to fetch notifications: " + e.getMessage());
        }
    }

    public void createTableIfNotExists() {
        try {
            dynamoDbClient.describeTable(DescribeTableRequest.builder()
                    .tableName(tableName).build());
            log.info("DynamoDB table '{}' already exists", tableName);
        } catch (ResourceNotFoundException e) {
            log.info("Creating DynamoDB table: {}", tableName);
            dynamoDbClient.createTable(CreateTableRequest.builder()
                    .tableName(tableName)
                    .keySchema(KeySchemaElement.builder()
                            .attributeName("id")
                            .keyType(KeyType.HASH)
                            .build())
                    .attributeDefinitions(AttributeDefinition.builder()
                            .attributeName("id")
                            .attributeType(ScalarAttributeType.S)
                            .build())
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build());
            log.info("DynamoDB table '{}' created successfully", tableName);
        }
    }
}