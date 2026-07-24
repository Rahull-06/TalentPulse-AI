package com.talentpulse.scoring;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentpulse.scoring.support.TestJwtFactory;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ScoringApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void computeScoreRankAndGenerateQuestions() throws Exception {
        UUID orgId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        String recruiterToken = TestJwtFactory.recruiterToken(UUID.randomUUID(), orgId);

        String computeBody = """
                {
                  "applicationId": "%s",
                  "jobId": "%s",
                  "organizationId": "%s",
                  "requiredSkills": ["Java", "Spring Boot"],
                  "preferredSkills": ["Kafka"],
                  "candidateSkills": ["Java", "Spring Boot"],
                  "resumeText": "Java backend engineer with Spring Boot experience",
                  "jobTitle": "Java Developer"
                }
                """.formatted(applicationId, jobId, orgId);

        MvcResult computeResult = mockMvc.perform(post("/api/v1/scoring/compute")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(computeBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.scoringMode").value("RULE_BASED"))
                .andExpect(jsonPath("$.fitScore").value(80.0))
                .andExpect(jsonPath("$.matchedSkills.length()").value(2))
                .andReturn();

        JsonNode score = objectMapper.readTree(computeResult.getResponse().getContentAsString());
        // required 100% * 0.8 + preferred 0% * 0.2 = 80
        org.assertj.core.api.Assertions.assertThat(score.get("fitScore").asDouble()).isEqualTo(80.0);

        mockMvc.perform(get("/api/v1/scoring/jobs/" + jobId + "/rankings")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        String questionsBody = """
                {
                  "jobId": "%s",
                  "jobTitle": "Java Developer",
                  "focusSkills": ["Spring Boot"],
                  "missingSkills": ["Kafka"]
                }
                """.formatted(jobId);

        mockMvc.perform(post("/api/v1/scoring/applications/" + applicationId + "/interview-questions")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(questionsBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.generatedBy").value("TEMPLATE"))
                .andExpect(jsonPath("$.questions").isArray());
    }

    @Test
    void computeWithoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/scoring/compute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rankingsAsCandidate_returns403() throws Exception {
        String candidateToken = TestJwtFactory.candidateToken(UUID.randomUUID());
        mockMvc.perform(get("/api/v1/scoring/jobs/" + UUID.randomUUID() + "/rankings")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isForbidden());
    }
}
