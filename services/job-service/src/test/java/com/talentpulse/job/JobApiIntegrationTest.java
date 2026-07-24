package com.talentpulse.job;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentpulse.job.support.TestJwtFactory;
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
class JobApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void recruiterCanCreatePublishAndPublicCanSearch() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        String token = TestJwtFactory.recruiterToken(userId, orgId);

        String body = """
                {
                  "title": "Spring Boot Engineer",
                  "description": "Build microservices with Java 21",
                  "location": "Remote",
                  "employmentType": "FULL_TIME",
                  "experienceMin": 2,
                  "experienceMax": 5,
                  "skills": [
                    { "skillName": "Java", "skillType": "REQUIRED", "weight": 5 },
                    { "skillName": "Spring Boot", "skillType": "REQUIRED", "weight": 5 }
                  ]
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/v1/jobs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.skills.length()").value(2))
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        String jobId = created.get("id").asText();

        mockMvc.perform(post("/api/v1/jobs/" + jobId + "/publish")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        mockMvc.perform(get("/api/v1/jobs").param("q", "Spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Spring Boot Engineer"));
    }

    @Test
    void candidateCannotCreateJob() throws Exception {
        String token = TestJwtFactory.candidateToken(UUID.randomUUID());

        String body = """
                {
                  "title": "Should Fail",
                  "description": "desc",
                  "location": "Pune",
                  "employmentType": "FULL_TIME",
                  "experienceMin": 1,
                  "experienceMax": 2,
                  "skills": [
                    { "skillName": "Java", "skillType": "REQUIRED", "weight": 1 }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/jobs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void createWithoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
