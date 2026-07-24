package com.talentpulse.candidate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentpulse.candidate.support.TestJwtFactory;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CandidateApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void candidateProfileResumeApplyAndRecruiterCanList() throws Exception {
        UUID candidateUserId = UUID.randomUUID();
        String candidateToken = TestJwtFactory.candidateToken(candidateUserId);

        mockMvc.perform(get("/api/v1/candidates/me")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(candidateUserId.toString()));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "fake-pdf-content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/candidates/me/resumes")
                        .file(file)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.primaryResume").value(true))
                .andExpect(jsonPath("$.fileName").value("resume.pdf"));

        UUID jobId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        String applyBody = """
                {
                  "jobId": "%s",
                  "organizationId": "%s",
                  "coverLetter": "I am excited to apply"
                }
                """.formatted(jobId, orgId);

        MvcResult applyResult = mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applyBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AI_SCORING"))
                .andExpect(jsonPath("$.jobId").value(jobId.toString()))
                .andReturn();

        objectMapper.readTree(applyResult.getResponse().getContentAsString());

        String recruiterToken = TestJwtFactory.recruiterToken(UUID.randomUUID(), orgId);

        mockMvc.perform(get("/api/v1/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void applyWithoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void duplicateApply_returns409() throws Exception {
        UUID candidateUserId = UUID.randomUUID();
        String token = TestJwtFactory.candidateToken(candidateUserId);

        mockMvc.perform(get("/api/v1/candidates/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        MockMultipartFile file = new MockMultipartFile(
                "file", "cv.pdf", "application/pdf", "data".getBytes()
        );
        mockMvc.perform(multipart("/api/v1/candidates/me/resumes")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        UUID jobId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        String body = """
                {"jobId":"%s","organizationId":"%s"}
                """.formatted(jobId, orgId);

        mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }
}
