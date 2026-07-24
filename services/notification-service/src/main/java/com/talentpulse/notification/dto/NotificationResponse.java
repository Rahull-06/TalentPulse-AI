package com.talentpulse.notification.dto;

import com.talentpulse.notification.enums.NotificationType;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponse {

    private UUID id;
    private UUID userId;
    private NotificationType type;
    private String title;
    private String message;
    private String link;
    private boolean read;
    private Instant createdAt;
}
