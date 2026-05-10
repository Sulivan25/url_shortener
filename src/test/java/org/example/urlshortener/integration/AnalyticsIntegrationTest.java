package org.example.urlshortener.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AnalyticsIntegrationTest extends BaseIntegrationTest {

    @BeforeEach
    void setUp() {
        stubRedis();
    }

    @Test
    void hourlyAnalytics_owner_returns200() throws Exception {
        String token = registerAndGetToken("analyticsOwner", "password123");
        String shortCode = createUrl(token, "https://analytics.com");

        mockMvc.perform(get("/api/short-urls/" + shortCode + "/analytics/hourly")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void hourlyAnalytics_nonOwner_returns403() throws Exception {
        String ownerToken = registerAndGetToken("aOwner", "password123");
        String otherToken = registerAndGetToken("aOther", "password123");
        String shortCode = createUrl(ownerToken, "https://private.com");

        mockMvc.perform(get("/api/short-urls/" + shortCode + "/analytics/hourly")
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void hourlyAnalytics_admin_canSeeAnyUrl() throws Exception {
        String userToken = registerAndGetToken("aUser", "password123");
        String shortCode = createUrl(userToken, "https://admin-can-see.com");

        String adminTkn = adminToken();

        mockMvc.perform(get("/api/short-urls/" + shortCode + "/analytics/hourly")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminTkn)))
                .andExpect(status().isOk());
    }

    // --------------- helper ---------------

    private String createUrl(String token, String originalUrl) throws Exception {
        String response = mockMvc.perform(post("/api/short-urls")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"originalUrl\":\"" + originalUrl + "\"}"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("shortCode").asText();
    }
}
