package org.example.urlshortener.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminUrlIntegrationTest extends BaseIntegrationTest {

    @BeforeEach
    void setUp() {
        stubRedis();
    }

    @Test
    void listAll_admin_returns200() throws Exception {
        String userToken = registerAndGetToken("auUser", "password123");
        createUrl(userToken, "https://a.com");
        createUrl(userToken, "https://b.com");

        String adminTkn = adminToken();

        mockMvc.perform(get("/admin/short-urls")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminTkn)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    void listAll_regularUser_returns403() throws Exception {
        String userToken = registerAndGetToken("auBlocked", "password123");

        mockMvc.perform(get("/admin/short-urls")
                        .header(HttpHeaders.AUTHORIZATION, bearer(userToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminExtend_anyUrl_succeeds() throws Exception {
        String userToken = registerAndGetToken("auExtUser", "password123");
        String shortCode = createUrl(userToken, "https://ext.com");

        String adminTkn = adminToken();

        mockMvc.perform(post("/admin/short-urls/" + shortCode + "/extend")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminTkn))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"days":10}
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void adminDelete_anyUrl_succeeds() throws Exception {
        String userToken = registerAndGetToken("auDelUser", "password123");
        String shortCode = createUrl(userToken, "https://del.com");

        String adminTkn = adminToken();

        mockMvc.perform(delete("/admin/short-urls/" + shortCode)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminTkn)))
                .andExpect(status().isNoContent());
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
