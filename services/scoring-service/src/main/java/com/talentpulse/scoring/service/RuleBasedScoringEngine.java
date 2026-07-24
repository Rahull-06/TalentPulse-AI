package com.talentpulse.scoring.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Deterministic skill matching — always available when AI is off/down.
 */
@Component
public class RuleBasedScoringEngine {

    public record RuleScoreResult(
            BigDecimal fitScore,
            List<String> matchedSkills,
            List<String> missingSkills,
            String explanation,
            String resumeSummary
    ) {
    }

    public RuleScoreResult score(
            List<String> requiredSkills,
            List<String> preferredSkills,
            List<String> candidateSkills,
            String resumeText,
            String jobTitle
    ) {
        List<String> required = normalize(requiredSkills);
        List<String> preferred = normalize(preferredSkills == null ? List.of() : preferredSkills);
        Set<String> candidate = new LinkedHashSet<>(normalize(candidateSkills));

        List<String> matchedRequired = required.stream().filter(candidate::contains).toList();
        List<String> missingRequired = required.stream().filter(s -> !candidate.contains(s)).toList();
        List<String> matchedPreferred = preferred.stream().filter(candidate::contains).toList();

        double requiredScore = required.isEmpty()
                ? 100.0
                : (matchedRequired.size() * 100.0) / required.size();
        double preferredScore = preferred.isEmpty()
                ? 100.0
                : (matchedPreferred.size() * 100.0) / preferred.size();

        // Required skills weigh more (80%) than preferred (20%)
        double fit = (requiredScore * 0.8) + (preferredScore * 0.2);
        BigDecimal fitScore = BigDecimal.valueOf(fit).setScale(2, RoundingMode.HALF_UP);

        List<String> matched = new ArrayList<>();
        matched.addAll(matchedRequired);
        matched.addAll(matchedPreferred);

        String explanation = buildExplanation(
                fitScore, matchedRequired.size(), required.size(),
                matchedPreferred.size(), preferred.size(), missingRequired
        );

        String summary = (resumeText == null || resumeText.isBlank())
                ? "No resume text provided; scored from skill lists only."
                : resumeText.length() <= 240 ? resumeText : resumeText.substring(0, 240) + "...";

        if (jobTitle != null && !jobTitle.isBlank()) {
            summary = "Candidate evaluated for '" + jobTitle + "'. " + summary;
        }

        return new RuleScoreResult(fitScore, matched, missingRequired, explanation, summary);
    }

    public List<String> generateTemplateQuestions(String jobTitle, List<String> focusSkills, List<String> missingSkills) {
        List<String> skills = focusSkills == null || focusSkills.isEmpty()
                ? (missingSkills == null ? List.of() : missingSkills)
                : focusSkills;

        List<String> questions = new ArrayList<>();
        String role = (jobTitle == null || jobTitle.isBlank()) ? "this role" : jobTitle;

        questions.add("Walk me through a recent project relevant to " + role + ".");
        if (!skills.isEmpty()) {
            questions.add("How have you used " + skills.get(0) + " in production?");
        }
        if (skills.size() > 1) {
            questions.add("Explain a challenge you solved involving " + skills.get(1) + ".");
        }
        if (missingSkills != null && !missingSkills.isEmpty()) {
            questions.add("You appear lighter on " + missingSkills.get(0)
                    + ". How would you ramp up quickly?");
        }
        questions.add("How do you approach debugging and delivering under deadlines?");
        return questions;
    }

    private List<String> normalize(List<String> skills) {
        return skills.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .distinct()
                .collect(Collectors.toList());
    }

    private String buildExplanation(
            BigDecimal fitScore,
            int matchedRequired,
            int totalRequired,
            int matchedPreferred,
            int totalPreferred,
            List<String> missingRequired
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("Fit score ").append(fitScore)
                .append(" based on rule-based matching. ")
                .append("Required skills matched: ")
                .append(matchedRequired).append("/").append(totalRequired).append(". ");
        if (totalPreferred > 0) {
            sb.append("Preferred skills matched: ")
                    .append(matchedPreferred).append("/").append(totalPreferred).append(". ");
        }
        if (!missingRequired.isEmpty()) {
            sb.append("Missing required skills: ")
                    .append(String.join(", ", missingRequired)).append(".");
        } else {
            sb.append("All required skills are covered.");
        }
        return sb.toString();
    }
}
