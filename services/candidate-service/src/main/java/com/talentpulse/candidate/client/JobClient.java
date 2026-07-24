package com.talentpulse.candidate.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Lightweight REST client to Job Service (public GET for published/draft visible jobs).
 */
@Component
@Slf4j
public class JobClient {

    private final RestClient restClient;

    public JobClient(@Value("${talentpulse.clients.job-service-url:http://localhost:8082}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @SuppressWarnings("unchecked")
    public JobSnapshot fetchJob(UUID jobId) {
        try {
            Map<String, Object> body = restClient.get()
                    .uri("/api/v1/jobs/{id}", jobId)
                    .retrieve()
                    .body(Map.class);
            if (body == null) {
                return JobSnapshot.empty();
            }
            String title = body.get("title") != null ? body.get("title").toString() : null;
            UUID createdBy = parseUuid(body.get("createdBy"));
            Integer maxApplicants = parseInteger(body.get("maxApplicants"));
            Integer openings = parseInteger(body.get("openings"));
            List<String> required = new java.util.ArrayList<>();
            List<String> preferred = new java.util.ArrayList<>();
            Object skillsObj = body.get("skills");
            if (skillsObj instanceof List<?> skills) {
                for (Object raw : skills) {
                    if (raw instanceof Map<?, ?> skill) {
                        Object name = skill.get("skillName");
                        Object type = skill.get("skillType");
                        if (name == null) {
                            continue;
                        }
                        if ("PREFERRED".equals(String.valueOf(type))) {
                            preferred.add(name.toString());
                        } else {
                            required.add(name.toString());
                        }
                    }
                }
            }
            return new JobSnapshot(title, createdBy, openings, maxApplicants, required, preferred);
        } catch (Exception ex) {
            log.warn("Could not fetch job {}: {}", jobId, ex.getMessage());
            return JobSnapshot.empty();
        }
    }

    private static UUID parseUuid(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return UUID.fromString(value.toString());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static Integer parseInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public record JobSnapshot(
            String title,
            UUID createdBy,
            Integer openings,
            Integer maxApplicants,
            List<String> requiredSkills,
            List<String> preferredSkills
    ) {
        static JobSnapshot empty() {
            return new JobSnapshot(null, null, null, null, Collections.emptyList(), Collections.emptyList());
        }
    }
}
