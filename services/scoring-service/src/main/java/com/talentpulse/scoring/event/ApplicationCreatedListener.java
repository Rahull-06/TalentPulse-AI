package com.talentpulse.scoring.event;

import com.talentpulse.scoring.dto.ScoreResultResponse;
import com.talentpulse.scoring.service.ScoringService;
import java.util.Collections;
import java.util.List;
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
public class ApplicationCreatedListener {

    private final ScoringService scoringService;
    private final DomainEventPublisher eventPublisher;

    @RabbitListener(queues = EventKeys.Q_APPLICATION_CREATED)
    public void onApplicationCreated(ApplicationCreatedEvent event) {
        if (event == null || event.getApplicationId() == null) {
            log.warn("Ignoring APPLICATION_CREATED with missing applicationId");
            return;
        }

        UUID applicationId = event.getApplicationId();
        log.info("APPLICATION_CREATED applicationId={}", applicationId);

        List<String> required = nonNullList(event.getRequiredSkills());
        List<String> preferred = nonNullList(event.getPreferredSkills());
        List<String> candidate = nonNullList(event.getCandidateSkills());

        if (required.isEmpty() && preferred.isEmpty()) {
            required = List.of("General");
        }

        ScoreResultResponse score = scoringService.computeFromEvent(
                applicationId,
                event.getJobId(),
                event.getOrganizationId(),
                required,
                preferred,
                candidate,
                event.getResumeText(),
                event.getJobTitle()
        );

        try {
            scoringService.generateQuestionsForApplication(
                    applicationId,
                    event.getJobId(),
                    event.getJobTitle(),
                    score.getMatchedSkills(),
                    score.getMissingSkills()
            );
        } catch (Exception ex) {
            log.warn("Could not auto-generate interview questions for {}", applicationId, ex);
        }

        try {
            eventPublisher.publish(
                    EventKeys.SCORE_COMPLETED,
                    ScoreCompletedEvent.builder()
                            .applicationId(applicationId)
                            .jobId(event.getJobId())
                            .organizationId(event.getOrganizationId())
                            .candidateUserId(event.getCandidateUserId())
                            .fitScore(score.getFitScore())
                            .scoringMode(score.getScoringMode() != null ? score.getScoringMode().name() : null)
                            .build()
            );
        } catch (Exception ex) {
            log.error("Failed to publish SCORE_COMPLETED for applicationId={}", applicationId, ex);
        }
    }

    private static List<String> nonNullList(List<String> values) {
        return values == null ? Collections.emptyList() : values;
    }
}
