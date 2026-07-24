package com.talentpulse.scoring.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class RuleBasedScoringEngineTest {

    private final RuleBasedScoringEngine engine = new RuleBasedScoringEngine();

    @Test
    void perfectRequiredMatch_scoresHigh() {
        var result = engine.score(
                List.of("Java", "Spring Boot"),
                List.of("Kafka"),
                List.of("java", "spring boot", "kafka"),
                "Experienced Java developer",
                "Backend Engineer"
        );

        assertThat(result.fitScore()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(result.missingSkills()).isEmpty();
        assertThat(result.matchedSkills()).contains("java", "spring boot", "kafka");
        assertThat(result.explanation()).contains("rule-based");
    }

    @Test
    void missingRequiredSkills_reducesScore() {
        var result = engine.score(
                List.of("Java", "Kafka"),
                List.of(),
                List.of("java"),
                null,
                "Java Role"
        );

        // 1/2 required * 80% + preferred 100%*20% = 40 + 20 = 60
        assertThat(result.fitScore()).isEqualByComparingTo(new BigDecimal("60.00"));
        assertThat(result.missingSkills()).containsExactly("kafka");
    }

    @Test
    void templateQuestions_includeFocusSkill() {
        List<String> questions = engine.generateTemplateQuestions(
                "Java Developer",
                List.of("Spring Boot"),
                List.of("Kafka")
        );

        assertThat(questions).isNotEmpty();
        assertThat(questions.stream().anyMatch(q -> q.contains("Spring Boot") || q.contains("Kafka")))
                .isTrue();
    }
}
