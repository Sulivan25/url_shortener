package org.example.urlshortener.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.urlshortener.domain.entity.ShortUrl;
import org.example.urlshortener.repository.ShortUrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RedirectIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ShortUrlRepository shortUrlRepository;

    @BeforeEach
    void setUp() {
        stubRedis();
    }

    @Test
    void redirect_validCode_returns302WithLocationHeader() throws Exception {
        String token = registerAndGetToken("redir1", "password123");
        String shortCode = createUrl(token, "https://google.com");

        mockMvc.perform(get("/" + shortCode))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, "https://google.com"));
    }

    @Test
    void redirect_expiredUrl_returns410() throws Exception {
        String token = registerAndGetToken("redir2", "password123");
        String shortCode = createUrl(token, "https://expired.com");

        // Manually expire the URL in the DB
        ShortUrl url = shortUrlRepository.findByShortCode(shortCode).orElseThrow();
        url.setExpireAt(LocalDateTime.now().minusDays(1));
        shortUrlRepository.save(url);

        mockMvc.perform(get("/" + shortCode))
                .andExpect(status().isGone());
    }

    @Test
    void redirect_nonexistentCode_returns404() throws Exception {
        mockMvc.perform(get("/nonexistent"))
                .andExpect(status().isNotFound());
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
