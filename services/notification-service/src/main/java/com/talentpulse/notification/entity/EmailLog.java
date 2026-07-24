package com.talentpulse.notification.entity;

import com.talentpulse.notification.enums.EmailStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Email delivery audit log (actual SMTP can be wired later).
 */
@Entity
@Table(name = "email_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailLog extends BaseEntity {

    @Column(nullable = false, length = 180)
    private String toEmail;

    @Column(nullable = false, length = 250)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmailStatus status;

    @Column(columnDefinition = "TEXT")
    private String providerResponse;

    /** Optional link to related notification */
    private UUID notificationId;
}
