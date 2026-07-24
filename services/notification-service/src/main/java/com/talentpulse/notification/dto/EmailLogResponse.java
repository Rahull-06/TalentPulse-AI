package com.talentpulse.notification.dto;

import com.talentpulse.notification.enums.EmailStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailLogResponse {

    private UUID id;
    private String toEmail;
    private String subject;
    private EmailStatus status;
    private String providerResponse;
    private UUID notificationId;
    private Instant createdAt;
}
