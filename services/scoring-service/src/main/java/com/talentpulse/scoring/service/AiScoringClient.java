package com.talentpulse.scoring.service;

import com.talentpulse.scoring.config.ScoringProperties;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Optional AI layer.
 * v1: stub that returns empty → caller falls back to rules.
 * Later: call Gemini with Spring AI / REST when ai-enabled + api-key are set.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AiScoringClient {

    private final ScoringProperties scoringProperties;

    public Optional<AiScoreResult> tryScore(
            List<String> requiredSkills,
            List<String> preferredSkills,
            List<String> candidateSkills,
            String resumeText,
            String jobTitle
    ) {
        if (!scoringProperties.isAiEnabled()) {
            return Optional.empty();
        }
        if (scoringProperties.getGemini().getApiKey() == null
                || scoringProperties.getGemini().getApiKey().isBlank()) {
            log.warn("AI enabled but GEMINI_API_KEY missing — using rule-based scoring");
            return Optional.empty();
        }

        // Placeholder for Gemini integration (Spring AI / HTTP).
        // Returning empty keeps production safe until API wiring is added.
        log.info("AI scoring requested (model={}) — Gemini client not wired yet; falling back",
                scoringProperties.getGemini().getModel());
        return Optional.empty();
    }

    public Optional<List<String>> tryGenerateQuestions(
            String jobTitle,
            List<String> focusSkills,
            List<String> missingSkills
    ) {
        if (!scoringProperties.isAiEnabled()
                || scoringProperties.getGemini().getApiKey() == null
                || scoringProperties.getGemini().getApiKey().isBlank()) {
            return Optional.empty();
        }
        log.info("AI questions requested — Gemini client not wired yet; using templates");
        return Optional.empty();
    }

    public record AiScoreResult(
            java.math.BigDecimal fitScore,
            List<String> matchedSkills,
            List<String> missingSkills,
            String explanation,
            String resumeSummary,
            String modelName
    ) {
    }
}
