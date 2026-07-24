package com.talentpulse.candidate.event;

import com.talentpulse.candidate.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "talentpulse.events.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class ScoreCompletedListener {

    private final ApplicationService applicationService;

    @RabbitListener(queues = EventKeys.Q_SCORE_COMPLETED)
    public void onScoreCompleted(ScoreCompletedEvent event) {
        if (event == null || event.getApplicationId() == null) {
            log.warn("Ignoring SCORE_COMPLETED with missing applicationId");
            return;
        }
        log.info("SCORE_COMPLETED applicationId={} fit={}", event.getApplicationId(), event.getFitScore());
        applicationService.markRecruiterReview(event.getApplicationId());
    }
}
