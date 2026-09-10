package com.loanunderwriting.notification.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDTO {

    private String id;
    private String applicationId;
    private String applicantName;
    private String email;
    private BigDecimal loanAmount;
    private String loanPurpose;
    private String status;
    private String message;
    private String createdAt;
}