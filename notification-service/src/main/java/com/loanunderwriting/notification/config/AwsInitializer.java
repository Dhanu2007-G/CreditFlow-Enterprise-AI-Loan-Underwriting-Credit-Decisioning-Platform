package com.loanunderwriting.notification.config;

import com.loanunderwriting.notification.service.DynamoDbService;
import com.loanunderwriting.notification.service.S3Service;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AwsInitializer {

    private final DynamoDbService dynamoDbService;
    private final S3Service s3Service;

    @PostConstruct
    public void initializeAwsResources() {
        log.info("Initializing AWS resources for loan underwriting...");
        dynamoDbService.createTableIfNotExists();
        s3Service.createBucketIfNotExists();
    }
}