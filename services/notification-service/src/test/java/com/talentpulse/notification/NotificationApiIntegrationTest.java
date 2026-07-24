package com.talentpulse.notification;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentpulse.notification.support.TestJwtFactory;
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
class NotificationApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createListMarkReadAndMarkAllRead() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = TestJwtFactory.candidateToken(userId);

        String body1 = """
                {
                  "userId": "%s",
                  "type": "APPLICATION_STATUS_CHANGED",
                  "title": "You were shortlisted",
                  "message": "Congrats — move to interview prep.",
                  "link": "/applications/1",
                  "toEmail": "candidate@test.com"
                }
                """.formatted(userId);

        MvcResult created = mockMvc.perform(post("/api/v1/notifications")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.read").value(false))
                .andExpect(jsonPath("$.title").value("You were shortlisted"))
                .andReturn();

        JsonNode notification = objectMapper.readTree(created.getResponse().getContentAsString());
        String notificationId = notification.get("id").asText();

        String body2 = """
                {
                  "userId": "%s",
                  "type": "GENERIC",
                  "title": "Welcome",
                  "message": "Thanks for joining TalentPulse."
                }
                """.formatted(userId);

        mockMvc.perform(post("/api/v1/notifications")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body2))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/notifications/me/unread-count")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(2));

        mockMvc.perform(get("/api/v1/notifications/me")
                        .header("Authorization", "Bearer " + token)
                        .param("unreadOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));

        mockMvc.perform(post("/api/v1/notifications/" + notificationId + "/read")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));

        mockMvc.perform(get("/api/v1/notifications/me/unread-count")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(1));

        mockMvc.perform(post("/api/v1/notifications/me/read-all")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Marked 1 notification(s) as read"));

        mockMvc.perform(get("/api/v1/notifications/me/unread-count")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(0));
    }

    @Test
    void markRead_otherUsersNotification_returns404() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        String ownerToken = TestJwtFactory.candidateToken(ownerId);
        String otherToken = TestJwtFactory.candidateToken(otherId);

        String body = """
                {
                  "userId": "%s",
                  "type": "GENERIC",
                  "title": "Private",
                  "message": "Only for owner"
                }
                """.formatted(ownerId);

        MvcResult created = mockMvc.perform(post("/api/v1/notifications")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        String notificationId = objectMapper.readTree(created.getResponse().getContentAsString())
                .get("id").asText();

        mockMvc.perform(post("/api/v1/notifications/" + notificationId + "/read")
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void listWithoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/me"))
                .andExpect(status().isUnauthorized());
    }
}
