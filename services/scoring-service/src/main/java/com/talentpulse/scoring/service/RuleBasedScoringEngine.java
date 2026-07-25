package com.talentpulse.scoring.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Deterministic skill matching — always available when AI is off/down.
 * Matches against candidate skill list and resume text (phrase / word-boundary).
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
        Map<String, String> requiredDisplay = displayMap(requiredSkills);
        Map<String, String> preferredDisplay = displayMap(preferredSkills == null ? List.of() : preferredSkills);
        List<String> required = List.copyOf(requiredDisplay.keySet());
        List<String> preferred = List.copyOf(preferredDisplay.keySet());

        Set<String> candidate = new LinkedHashSet<>(normalizeList(candidateSkills));
        String resumeHaystack = resumeText == null ? "" : resumeText.toLowerCase(Locale.ROOT);

        List<String> matchedRequiredNorm = new ArrayList<>();
        List<String> missingRequiredNorm = new ArrayList<>();
        for (String skill : required) {
            if (hasSkill(skill, candidate, resumeHaystack)) {
                matchedRequiredNorm.add(skill);
            } else {
                missingRequiredNorm.add(skill);
            }
        }

        List<String> matchedPreferredNorm = new ArrayList<>();
        for (String skill : preferred) {
            if (hasSkill(skill, candidate, resumeHaystack)) {
                matchedPreferredNorm.add(skill);
            }
        }

        double requiredScore = required.isEmpty()
                ? 100.0
                : (matchedRequiredNorm.size() * 100.0) / required.size();

        // Empty preferred must NOT inflate the score (previously awarded a free 20 points).
        double fit;
        if (preferred.isEmpty()) {
            fit = requiredScore;
        } else {
            double preferredScore = (matchedPreferredNorm.size() * 100.0) / preferred.size();
            fit = (requiredScore * 0.8) + (preferredScore * 0.2);
        }
        BigDecimal fitScore = BigDecimal.valueOf(fit).setScale(2, RoundingMode.HALF_UP);

        List<String> matched = new ArrayList<>();
        matchedRequiredNorm.forEach(s -> matched.add(requiredDisplay.get(s)));
        matchedPreferredNorm.forEach(s -> matched.add(preferredDisplay.get(s)));

        List<String> missing = missingRequiredNorm.stream().map(requiredDisplay::get).toList();

        String explanation = buildExplanation(
                fitScore, matchedRequiredNorm.size(), required.size(),
                matchedPreferredNorm.size(), preferred.size(), missing
        );

        String summary = buildResumeSummary(resumeText, jobTitle);

        return new RuleScoreResult(fitScore, matched, missing, explanation, summary);
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

    static boolean hasSkill(String normalizedSkill, Set<String> candidateSkills, String resumeLower) {
        if (candidateSkills.contains(normalizedSkill)) {
            return true;
        }
        if (resumeLower == null || resumeLower.isBlank()) {
            return false;
        }
        return resumeContainsSkill(resumeLower, normalizedSkill);
    }

    /**
     * Phrase match with loose word boundaries so "java" does not match "javascript",
     * but "spring boot" matches inside resume prose.
     */
    static boolean resumeContainsSkill(String resumeLower, String skillLower) {
        String escaped = Pattern.quote(skillLower);
        Pattern pattern = Pattern.compile("(?<![a-z0-9+#.])" + escaped + "(?![a-z0-9+#.])");
        return pattern.matcher(resumeLower).find();
    }

    private Map<String, String> displayMap(List<String> skills) {
        Map<String, String> map = new LinkedHashMap<>();
        if (skills == null) {
            return map;
        }
        for (String raw : skills) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String display = raw.trim();
            String key = display.toLowerCase(Locale.ROOT);
            map.putIfAbsent(key, display);
        }
        return map;
    }

    private List<String> normalizeList(List<String> skills) {
        if (skills == null) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (String raw : skills) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String key = raw.trim().toLowerCase(Locale.ROOT);
            if (seen.add(key)) {
                out.add(key);
            }
        }
        return out;
    }

    private String buildResumeSummary(String resumeText, String jobTitle) {
        String summary;
        if (resumeText == null || resumeText.isBlank()) {
            summary = "No resume text provided; scored from skill lists only.";
        } else if (resumeText.startsWith("Uploaded file:")) {
            summary = "Resume was not parsed (placeholder text only). Re-upload a PDF so skills can be read from the document.";
        } else {
            summary = resumeText.length() <= 280 ? resumeText : resumeText.substring(0, 280) + "...";
        }
        if (jobTitle != null && !jobTitle.isBlank()) {
            summary = "Candidate evaluated for '" + jobTitle + "'. " + summary;
        }
        return summary;
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
                .append(" based on rule-based matching against resume text and profile skills. ")
                .append("Required skills matched: ")
                .append(matchedRequired).append("/").append(totalRequired).append(". ");
        if (totalPreferred > 0) {
            sb.append("Preferred skills matched: ")
                    .append(matchedPreferred).append("/").append(totalPreferred).append(". ");
        }
        if (!missingRequired.isEmpty()) {
            sb.append("Missing required skills: ")
                    .append(String.join(", ", missingRequired)).append(".");
        } else if (totalRequired > 0) {
            sb.append("All required skills are covered.");
        }
        return sb.toString();
    }
}
