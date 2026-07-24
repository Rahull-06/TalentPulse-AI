package com.talentpulse.notification.dto;

import com.talentpulse.notification.enums.NotificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * v1: other services (or tests) create inbox items via REST.
 * Later: RabbitMQ consumers will call the same service layer.
 */
@Getter
@Setter
public class CreateNotificationRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private NotificationType type;

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    private String message;

    @Size(max = 500)
    private String link;

    /** If set, also write an email_logs row (SMTP still off in v1). */
    @Email
    @Size(max = 180)
    private String toEmail;
}
