package com.talentpulse.notification.event;

import com.talentpulse.notification.dto.CreateNotificationRequest;
import com.talentpulse.notification.enums.NotificationType;
import com.talentpulse.notification.service.NotificationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "talentpulse.events.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class DomainEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = EventKeys.Q_USER_REGISTERED)
    public void onUserRegistered(InboundEvents.UserRegistered payload) {
        if (payload == null) {
            return;
        }
        create(payload.getUserId(), NotificationType.USER_REGISTERED,
                "Welcome to TalentPulse",
                "Hi " + (payload.getFullName() != null ? payload.getFullName() : "there")
                        + " — your account is ready.",
                null);
    }

    @RabbitListener(queues = EventKeys.Q_JOB_PUBLISHED)
    public void onJobPublished(InboundEvents.JobPublished payload) {
        if (payload == null) {
            return;
        }
        String jobId = payload.getJobId() != null ? payload.getJobId().toString() : null;
        create(payload.getCreatedBy(), NotificationType.JOB_PUBLISHED,
                "Job published",
                "\"" + payload.getTitle() + "\" is now live.",
                jobId != null ? "/recruiter/jobs/" + jobId : null);
    }

    @RabbitListener(queues = EventKeys.Q_APPLICATION_CREATED)
    public void onApplicationCreated(InboundEvents.ApplicationCreated payload) {
        if (payload == null) {
            return;
        }
        String title = payload.getJobTitle() != null ? payload.getJobTitle() : "a role";
        create(payload.getCandidateUserId(), NotificationType.APPLICATION_CREATED,
                "Application received",
                "We received your application for \"" + title + "\" and started screening.",
                "/candidate/applications");
        create(payload.getRecruiterUserId(), NotificationType.APPLICATION_CREATED,
                "New applicant",
                "A candidate applied to \"" + title + "\". Review them in Pipeline.",
                "/recruiter/jobs");
    }

    @RabbitListener(queues = EventKeys.Q_STATUS_CHANGED)
    public void onStatusChanged(InboundEvents.ApplicationStatusChanged payload) {
        if (payload == null) {
            return;
        }
        String status = payload.getToStatus() != null ? payload.getToStatus() : "updated";
        String note = payload.getNote();
        String message;
        if ("REJECTED".equalsIgnoreCase(status)) {
            message = "Your application was not selected."
                    + (note != null && !note.isBlank() ? " Reason: " + note : "")
                    + " Open Applications to see skill-gap feedback when a score is available.";
        } else {
            message = "Your application status is now " + status + "."
                    + (note != null && !note.isBlank() ? " Note: " + note : "");
        }
        create(payload.getCandidateUserId(), NotificationType.APPLICATION_STATUS_CHANGED,
                "Application update",
                message,
                "/candidate/applications");
    }

    @RabbitListener(queues = EventKeys.Q_SCORE_COMPLETED)
    public void onScoreCompleted(InboundEvents.ScoreCompleted payload) {
        if (payload == null) {
            return;
        }
        String fit = payload.getFitScore() != null ? payload.getFitScore().toPlainString() : null;
        create(payload.getCandidateUserId(), NotificationType.SCORE_COMPLETED,
                "Fit score ready",
                "Your application was scored" + (fit != null ? " (" + fit + ")" : "")
                        + ". A recruiter will review next.",
                "/candidate/applications");
    }

    private void create(UUID userId, NotificationType type, String title, String message, String link) {
        if (userId == null) {
            log.warn("Skipping notification — missing userId for {}", type);
            return;
        }
        log.info("Creating notification type={} userId={}", type, userId);
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(userId);
        request.setType(type);
        request.setTitle(title);
        request.setMessage(message);
        request.setLink(link);
        notificationService.create(request);
    }
}
