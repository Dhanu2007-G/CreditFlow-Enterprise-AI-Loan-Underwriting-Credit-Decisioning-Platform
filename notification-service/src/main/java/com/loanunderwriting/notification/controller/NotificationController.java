package com.loanunderwriting.notification.controller;

import com.loanunderwriting.notification.dto.NotificationResponseDTO;
import com.loanunderwriting.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification API",
        description = "Endpoints for loan notifications and audit logs")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get all notifications")
    public ResponseEntity<List<NotificationResponseDTO>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @GetMapping("/application/{applicationId}")
    @Operation(summary = "Get notifications by application ID")
    public ResponseEntity<List<NotificationResponseDTO>>
    getNotificationsByApplicationId(
            @PathVariable String applicationId) {
        return ResponseEntity.ok(
                notificationService.getNotificationsByApplicationId(applicationId));
    }
}