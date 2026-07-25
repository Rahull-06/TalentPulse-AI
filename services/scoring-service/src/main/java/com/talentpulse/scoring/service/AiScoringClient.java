package com.talentpulse.scoring.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentpulse.scoring.config.ScoringProperties;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Calls Gemini to score a resume against job skills.
 * Falls back to empty Optional when disabled, missing key, or any API/parse failure.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AiScoringClient {

    private final ScoringProperties scoringProperties;
    private final ObjectMapper objectMapper;

    public Optional<AiScoreResult> tryScore(
            List<String> requiredSkills,
            List<String> preferredSkills,
            List<String> candidateSkills,
            String resumeText,
            String jobTitle
    ) {
        if (!canCallGemini()) {
            return Optional.empty();
        }
        if (resumeText == null || resumeText.isBlank() || resumeText.startsWith("Uploaded file:")) {
            log.info("Skipping AI scoring — resume text missing or not parsed");
            return Optional.empty();
        }

        try {
            String prompt = buildScorePrompt(requiredSkills, preferredSkills, candidateSkills, resumeText, jobTitle);
            String raw = callGemini(prompt);
            return parseScoreResponse(raw, scoringProperties.getGemini().getModel());
        } catch (Exception ex) {
            log.warn("AI scoring failed — falling back to rules: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    public Optional<List<String>> tryGenerateQuestions(
            String jobTitle,
            List<String> focusSkills,
            List<String> missingSkills
    ) {
        if (!canCallGemini()) {
            return Optional.empty();
        }
        try {
            String prompt = """
                    You are a hiring manager. Return ONLY a JSON array of 4-6 interview question strings.
                    Role: %s
                    Focus skills: %s
                    Skill gaps: %s
                    """.formatted(
                    nullToEmpty(jobTitle),
                    String.join(", ", safeList(focusSkills)),
                    String.join(", ", safeList(missingSkills))
            );
            String raw = callGemini(prompt);
            String json = extractJson(raw);
            JsonNode node = objectMapper.readTree(json);
            if (!node.isArray()) {
                return Optional.empty();
            }
            List<String> questions = new ArrayList<>();
            node.forEach(n -> {
                if (n.isTextual() && !n.asText().isBlank()) {
                    questions.add(n.asText().trim());
                }
            });
            return questions.isEmpty() ? Optional.empty() : Optional.of(questions);
        } catch (Exception ex) {
            log.warn("AI questions failed — using templates: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private boolean canCallGemini() {
        if (!scoringProperties.isAiEnabled()) {
            return false;
        }
        String key = scoringProperties.getGemini().getApiKey();
        if (key == null || key.isBlank()) {
            log.debug("AI enabled but GEMINI_API_KEY missing — using rule-based scoring");
            return false;
        }
        return true;
    }

    private String buildScorePrompt(
            List<String> requiredSkills,
            List<String> preferredSkills,
            List<String> candidateSkills,
            String resumeText,
            String jobTitle
    ) {
        String clippedResume = resumeText.length() > 12000 ? resumeText.substring(0, 12000) : resumeText;
        return """
                You are an expert technical recruiter. Analyze the resume against the job skills.
                Return ONLY valid JSON (no markdown) with this exact shape:
                {
                  "fitScore": <number 0-100 with up to 2 decimals>,
                  "matchedSkills": ["skill", ...],
                  "missingSkills": ["skill", ...],
                  "explanation": "short recruiter-facing rationale",
                  "resumeSummary": "2-3 sentence summary of the candidate for this role"
                }

                Rules:
                - matchedSkills / missingSkills must use names from the required+preferred lists when possible.
                - missingSkills = required skills not evidenced in the resume.
                - Score required skills ~80%% weight and preferred ~20%%. If preferred is empty, score from required only.
                - Be evidence-based: only mark a skill matched if the resume clearly supports it.
                - Do not invent work history.

                Job title: %s
                Required skills: %s
                Preferred skills: %s
                Profile skills (may be incomplete): %s

                Resume text:
                ---
                %s
                ---
                """.formatted(
                nullToEmpty(jobTitle),
                String.join(", ", safeList(requiredSkills)),
                String.join(", ", safeList(preferredSkills)),
                String.join(", ", safeList(candidateSkills)),
                clippedResume
        );
    }

    private String callGemini(String prompt) {
        String model = scoringProperties.getGemini().getModel();
        if (model == null || model.isBlank()) {
            model = "gemini-2.0-flash";
        }
        String apiKey = scoringProperties.getGemini().getApiKey();
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model
                + ":generateContent?key="
                + apiKey;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(15));
        requestFactory.setReadTimeout(Duration.ofSeconds(45));

        RestClient client = RestClient.builder()
                .requestFactory(requestFactory)
                .build();

        String body = """
                {
                  "contents": [{ "parts": [{ "text": %s }] }],
                  "generationConfig": {
                    "temperature": 0.2,
                    "responseMimeType": "application/json"
                  }
                }
                """.formatted(objectMapper.valueToTree(prompt).toString());

        String response = client.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
            if (!textNode.isTextual() || textNode.asText().isBlank()) {
                throw new IllegalStateException("Gemini returned empty content");
            }
            return textNode.asText();
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse Gemini response: " + ex.getMessage(), ex);
        }
    }

    private Optional<AiScoreResult> parseScoreResponse(String raw, String modelName) throws Exception {
        String json = extractJson(raw);
        JsonNode node = objectMapper.readTree(json);

        BigDecimal fit = node.path("fitScore").decimalValue();
        if (fit.compareTo(BigDecimal.ZERO) < 0) {
            fit = BigDecimal.ZERO;
        }
        if (fit.compareTo(new BigDecimal("100")) > 0) {
            fit = new BigDecimal("100");
        }
        fit = fit.setScale(2, RoundingMode.HALF_UP);

        List<String> matched = readStringArray(node.path("matchedSkills"));
        List<String> missing = readStringArray(node.path("missingSkills"));
        String explanation = node.path("explanation").asText("AI scored this application from the resume.");
        String summary = node.path("resumeSummary").asText("AI resume summary unavailable.");

        return Optional.of(new AiScoreResult(fit, matched, missing, explanation, summary, modelName));
    }

    private List<String> readStringArray(JsonNode node) {
        List<String> out = new ArrayList<>();
        if (node != null && node.isArray()) {
            node.forEach(n -> {
                if (n.isTextual() && !n.asText().isBlank()) {
                    out.add(n.asText().trim());
                }
            });
        }
        return out;
    }

    private String extractJson(String raw) {
        String trimmed = raw.trim();
        if (trimmed.startsWith("```")) {
            int firstNl = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNl > 0 && lastFence > firstNl) {
                trimmed = trimmed.substring(firstNl + 1, lastFence).trim();
            }
        }
        int objStart = trimmed.indexOf('{');
        int objEnd = trimmed.lastIndexOf('}');
        int arrStart = trimmed.indexOf('[');
        int arrEnd = trimmed.lastIndexOf(']');
        if (objStart >= 0 && objEnd > objStart && (arrStart < 0 || objStart < arrStart)) {
            return trimmed.substring(objStart, objEnd + 1);
        }
        if (arrStart >= 0 && arrEnd > arrStart) {
            return trimmed.substring(arrStart, arrEnd + 1);
        }
        return trimmed;
    }

    private List<String> safeList(List<String> values) {
        return values == null ? List.of() : values;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public record AiScoreResult(
            BigDecimal fitScore,
            List<String> matchedSkills,
            List<String> missingSkills,
            String explanation,
            String resumeSummary,
            String modelName
    ) {
    }
}
