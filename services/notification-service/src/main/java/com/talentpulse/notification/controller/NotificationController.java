package com.talentpulse.notification.controller;

import com.talentpulse.notification.dto.CreateNotificationRequest;
import com.talentpulse.notification.dto.MessageResponse;
import com.talentpulse.notification.dto.NotificationResponse;
import com.talentpulse.notification.dto.PageResponse;
import com.talentpulse.notification.dto.UnreadCountResponse;
import com.talentpulse.notification.security.AuthPrincipal;
import com.talentpulse.notification.service.NotificationService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** v1: other services / tests create inbox items. Later: RabbitMQ. */
    @PostMapping
    public ResponseEntity<NotificationResponse> create(
            @Valid @RequestBody CreateNotificationRequest request,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.create(request));
    }

    @GetMapping("/me")
    public ResponseEntity<PageResponse<NotificationResponse>> listMine(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                notificationService.listMine(principal, unreadOnly, page, size)
        );
    }

    @GetMapping("/me/unread-count")
    public ResponseEntity<UnreadCountResponse> unreadCount(
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return ResponseEntity.ok(notificationService.unreadCount(principal));
    }

    @PostMapping("/me/read-all")
    public ResponseEntity<MessageResponse> markAllRead(
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return ResponseEntity.ok(notificationService.markAllRead(principal));
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markRead(
            @PathVariable UUID notificationId,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return ResponseEntity.ok(notificationService.markRead(notificationId, principal));
    }
}
