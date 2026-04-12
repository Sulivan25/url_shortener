package org.example.urlshortener.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ShortUrlCrudIntegrationTest extends BaseIntegrationTest {

    @BeforeEach
    void setUp() {
        stubRedis();
    }

    // -------- Create --------

    @Test
    void create_authenticated_returnsShortCode() throws Exception {
        String token = registerAndGetToken("user1", "password123");

        mockMvc.perform(post("/api/short-urls")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"originalUrl":"https://example.com"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").isNotEmpty())
                .andExpect(jsonPath("$.originalUrl").value("https://example.com"))
                .andExpect(jsonPath("$.owner").value("user1"));
    }

    @Test
    void create_withExpireDays_setsExpiration() throws Exception {
        String token = registerAndGetToken("user2", "password123");

        mockMvc.perform(post("/api/short-urls")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"originalUrl":"https://example.com","expireDays":7}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expireAt").isNotEmpty());
    }

    @Test
    void create_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/short-urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"originalUrl":"https://example.com"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    // -------- List mine --------

    @Test
    void listMine_returnsOnlyOwnUrls() throws Exception {
        String tokenA = registerAndGetToken("ownerA", "password123");
        String tokenB = registerAndGetToken("ownerB", "password123");

        // ownerA creates 2 URLs
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/short-urls")
                    .header(HttpHeaders.AUTHORIZATION, bearer(tokenA))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"originalUrl\":\"https://a" + i + ".com\"}"));
        }
        // ownerB creates 1 URL
        mockMvc.perform(post("/api/short-urls")
                .header(HttpHeaders.AUTHORIZATION, bearer(tokenB))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"originalUrl\":\"https://b.com\"}"));

        mockMvc.perform(get("/api/short-urls/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));

        mockMvc.perform(get("/api/short-urls/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    // -------- Extend --------

    @Test
    void extend_owner_succeeds() throws Exception {
        String token = registerAndGetToken("extUser", "password123");

        String shortCode = createUrlAndGetCode(token, "https://extend.com", 3);

        mockMvc.perform(post("/api/short-urls/" + shortCode + "/extend")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"days":5}
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void extend_nonOwner_returns403() throws Exception {
        String ownerToken = registerAndGetToken("extOwner", "password123");
        String otherToken = registerAndGetToken("extOther", "password123");

        String shortCode = createUrlAndGetCode(ownerToken, "https://owned.com", 3);

        mockMvc.perform(post("/api/short-urls/" + shortCode + "/extend")
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"days":5}
                                """))
                .andExpect(status().isForbidden());
    }

    // -------- Delete --------

    @Test
    void delete_owner_succeeds() throws Exception {
        String token = registerAndGetToken("delUser", "password123");

        String shortCode = createUrlAndGetCode(token, "https://delete.com", null);

        mockMvc.perform(delete("/api/short-urls/" + shortCode)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_nonOwner_returns403() throws Exception {
        String ownerToken = registerAndGetToken("delOwner", "password123");
        String otherToken = registerAndGetToken("delOther", "password123");

        String shortCode = createUrlAndGetCode(ownerToken, "https://owned2.com", null);

        mockMvc.perform(delete("/api/short-urls/" + shortCode)
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isForbidden());
    }

    // --------------- helper ---------------

    private String createUrlAndGetCode(String token, String url, Integer expireDays) throws Exception {
        String body = expireDays != null
                ? "{\"originalUrl\":\"" + url + "\",\"expireDays\":" + expireDays + "}"
                : "{\"originalUrl\":\"" + url + "\"}";

        String response = mockMvc.perform(post("/api/short-urls")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("shortCode").asText();
    }
}
