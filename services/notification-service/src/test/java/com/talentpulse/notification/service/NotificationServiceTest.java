package com.talentpulse.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.talentpulse.notification.config.MailProperties;
import com.talentpulse.notification.dto.CreateNotificationRequest;
import com.talentpulse.notification.dto.NotificationResponse;
import com.talentpulse.notification.entity.EmailLog;
import com.talentpulse.notification.entity.Notification;
import com.talentpulse.notification.enums.EmailStatus;
import com.talentpulse.notification.enums.NotificationType;
import com.talentpulse.notification.repository.EmailLogRepository;
import com.talentpulse.notification.repository.NotificationRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private EmailLogRepository emailLogRepository;
    @Mock private MailProperties mailProperties;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        when(mailProperties.isEnabled()).thenReturn(false);
    }

    @Test
    void create_withToEmail_writesSkippedEmailLog() {
        UUID userId = UUID.randomUUID();
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(userId);
        request.setType(NotificationType.JOB_PUBLISHED);
        request.setTitle("New role");
        request.setMessage("A matching job was published.");
        request.setToEmail("user@test.com");

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            n.setId(UUID.randomUUID());
            return n;
        });
        when(emailLogRepository.save(any(EmailLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse response = notificationService.create(request);

        assertThat(response.getTitle()).isEqualTo("New role");
        assertThat(response.isRead()).isFalse();

        ArgumentCaptor<EmailLog> emailCaptor = ArgumentCaptor.forClass(EmailLog.class);
        verify(emailLogRepository).save(emailCaptor.capture());
        assertThat(emailCaptor.getValue().getStatus()).isEqualTo(EmailStatus.SKIPPED);
        assertThat(emailCaptor.getValue().getToEmail()).isEqualTo("user@test.com");
    }
}
