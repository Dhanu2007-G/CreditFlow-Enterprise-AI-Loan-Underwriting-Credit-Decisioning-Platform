package com.loanunderwriting.notification.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRecord {

    private String id;
    private String applicationId;
    private String applicantName;
    private String email;
    private BigDecimal loanAmount;
    private String loanPurpose;
    private String status;
    private String message;
    private LocalDateTime createdAt;
}