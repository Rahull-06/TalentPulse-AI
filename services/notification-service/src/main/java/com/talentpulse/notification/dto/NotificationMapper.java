package com.talentpulse.notification.dto;

import com.talentpulse.notification.entity.EmailLog;
import com.talentpulse.notification.entity.Notification;
import java.util.List;
import org.springframework.data.domain.Page;

public final class NotificationMapper {

    private NotificationMapper() {
    }

    public static NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .link(notification.getLink())
                .read(notification.isReadFlag())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    public static EmailLogResponse toEmailLogResponse(EmailLog log) {
        return EmailLogResponse.builder()
                .id(log.getId())
                .toEmail(log.getToEmail())
                .subject(log.getSubject())
                .status(log.getStatus())
                .providerResponse(log.getProviderResponse())
                .notificationId(log.getNotificationId())
                .createdAt(log.getCreatedAt())
                .build();
    }

    public static PageResponse<NotificationResponse> toPage(Page<Notification> page) {
        List<NotificationResponse> content = page.getContent().stream()
                .map(NotificationMapper::toResponse)
                .toList();
        return PageResponse.<NotificationResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
