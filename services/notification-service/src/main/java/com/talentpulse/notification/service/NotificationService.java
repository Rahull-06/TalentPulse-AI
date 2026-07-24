package com.talentpulse.notification.service;

import com.talentpulse.notification.config.MailProperties;
import com.talentpulse.notification.dto.CreateNotificationRequest;
import com.talentpulse.notification.dto.MessageResponse;
import com.talentpulse.notification.dto.NotificationMapper;
import com.talentpulse.notification.dto.NotificationResponse;
import com.talentpulse.notification.dto.PageResponse;
import com.talentpulse.notification.dto.UnreadCountResponse;
import com.talentpulse.notification.entity.EmailLog;
import com.talentpulse.notification.entity.Notification;
import com.talentpulse.notification.enums.EmailStatus;
import com.talentpulse.notification.exception.ResourceNotFoundException;
import com.talentpulse.notification.repository.EmailLogRepository;
import com.talentpulse.notification.repository.NotificationRepository;
import com.talentpulse.notification.security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailLogRepository emailLogRepository;
    private final MailProperties mailProperties;

    /**
     * Create an in-app notification. Optional toEmail → email_logs row (SMTP later).
     */
    @Transactional
    public NotificationResponse create(CreateNotificationRequest request) {
        Notification notification = notificationRepository.save(
                Notification.builder()
                        .userId(request.getUserId())
                        .type(request.getType())
                        .title(request.getTitle().trim())
                        .message(request.getMessage().trim())
                        .link(trimToNull(request.getLink()))
                        .readFlag(false)
                        .build()
        );

        if (StringUtils.hasText(request.getToEmail())) {
            writeEmailLog(notification, request.getToEmail().trim(), request.getTitle().trim());
        }

        return NotificationMapper.toResponse(notification);
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> listMine(
            AuthPrincipal principal,
            boolean unreadOnly,
            int page,
            int size
    ) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        Page<Notification> result = unreadOnly
                ? notificationRepository.findByUserIdAndReadFlagFalseOrderByCreatedAtDesc(
                        principal.userId(), pageable)
                : notificationRepository.findByUserIdOrderByCreatedAtDesc(
                        principal.userId(), pageable);
        return NotificationMapper.toPage(result);
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse unreadCount(AuthPrincipal principal) {
        long count = notificationRepository.countByUserIdAndReadFlagFalse(principal.userId());
        return UnreadCountResponse.builder().unreadCount(count).build();
    }

    @Transactional
    public NotificationResponse markRead(java.util.UUID notificationId, AuthPrincipal principal) {
        Notification notification = notificationRepository
                .findByIdAndUserId(notificationId, principal.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.isReadFlag()) {
            notification.setReadFlag(true);
            notification = notificationRepository.save(notification);
        }
        return NotificationMapper.toResponse(notification);
    }

    @Transactional
    public MessageResponse markAllRead(AuthPrincipal principal) {
        int updated = notificationRepository.markAllRead(principal.userId());
        return new MessageResponse("Marked " + updated + " notification(s) as read");
    }

    private void writeEmailLog(Notification notification, String toEmail, String subject) {
        EmailStatus status;
        String providerResponse;

        if (!mailProperties.isEnabled()) {
            status = EmailStatus.SKIPPED;
            providerResponse = "Mail disabled (v1 log-only)";
            log.info("Email skipped (mail.enabled=false) to={} subject={}", toEmail, subject);
        } else {
            // SMTP not wired yet — queue for future sender
            status = EmailStatus.QUEUED;
            providerResponse = "Queued (SMTP not configured)";
            log.info("Email queued to={} subject={}", toEmail, subject);
        }

        emailLogRepository.save(
                EmailLog.builder()
                        .toEmail(toEmail)
                        .subject(subject)
                        .status(status)
                        .providerResponse(providerResponse)
                        .notificationId(notification.getId())
                        .build()
        );
    }

    private static String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
