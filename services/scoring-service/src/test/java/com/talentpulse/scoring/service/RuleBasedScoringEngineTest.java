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
        assertThat(result.matchedSkills()).contains("Java", "Spring Boot", "Kafka");
    }

    @Test
    void missingRequiredSkills_withEmptyPreferred_doesNotInflateScore() {
        var result = engine.score(
                List.of("Java", "Kafka"),
                List.of(),
                List.of("java"),
                null,
                "Java Role"
        );

        // 1/2 required only (no free preferred points) = 50
        assertThat(result.fitScore()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result.missingSkills()).containsExactly("Kafka");
    }

    @Test
    void emptyCandidateSkillsAndEmptyPreferred_scoresZeroNotTwenty() {
        var result = engine.score(
                List.of("Java", "Spring Boot"),
                List.of(),
                List.of(),
                null,
                "Java Developer"
        );

        assertThat(result.fitScore()).isEqualByComparingTo(new BigDecimal("0.00"));
        assertThat(result.matchedSkills()).isEmpty();
        assertThat(result.missingSkills()).containsExactly("Java", "Spring Boot");
    }

    @Test
    void matchesSkillsFoundInResumeText() {
        var result = engine.score(
                List.of("Java", "Spring Boot", "Kafka"),
                List.of(),
                List.of(),
                "Senior engineer with deep Java and Spring Boot experience building APIs.",
                "Java Developer"
        );

        assertThat(result.fitScore()).isEqualByComparingTo(new BigDecimal("66.67"));
        assertThat(result.matchedSkills()).containsExactly("Java", "Spring Boot");
        assertThat(result.missingSkills()).containsExactly("Kafka");
        assertThat(result.explanation()).contains("2/3");
    }

    @Test
    void javaDoesNotMatchJavascript() {
        var result = engine.score(
                List.of("Java"),
                List.of(),
                List.of(),
                "Frontend developer skilled in JavaScript and React.",
                "Java Role"
        );

        assertThat(result.fitScore()).isEqualByComparingTo(new BigDecimal("0.00"));
        assertThat(result.missingSkills()).containsExactly("Java");
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
