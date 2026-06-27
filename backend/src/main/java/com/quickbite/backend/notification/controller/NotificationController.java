package com.quickbite.backend.notification.controller;

import com.quickbite.backend.common.ApiResponse;
import com.quickbite.backend.notification.dto.NotificationResponse;
import com.quickbite.backend.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/v1/notifications")
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "Notification Controller", description = "Endpoints for retrieving history, counting unread alerts, and marking alerts as read")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get notification history", description = "Retrieves a paginated list of all past alerts, system messages, and status updates")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getHistory(Pageable pageable, Principal principal) {
        log.info("Fetching notification history for: {}", principal.getName());
        Page<NotificationResponse> response = notificationService.getHistory(principal.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Notification history fetched successfully.", response));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread alert count", description = "Fetches the total count of unread notifications for the user")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(Principal principal) {
        log.debug("Fetching unread count for: {}", principal.getName());
        long count = notificationService.getUnreadCount(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Unread count fetched successfully.", count));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark alert as read", description = "Updates a specific notification's status flag to read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable Long id, Principal principal) {
        log.info("Marking notification ID {} as read by: {}", id, principal.getName());
        NotificationResponse response = notificationService.markAsRead(principal.getName(), id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read successfully.", response));
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark all alerts as read", description = "Updates all unread notifications for the authenticated user to read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(Principal principal) {
        log.info("Marking all notifications as read for: {}", principal.getName());
        notificationService.markAllAsRead(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read successfully."));
    }
}
