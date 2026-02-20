package org.example.urlshortener.controller;

import org.example.urlshortener.domain.entity.ShortUrl;
import org.example.urlshortener.repository.ShortUrlRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RedirectControllerIT {

    @Autowired
    private final MockMvc mockMvc;

    @Autowired
    private final ShortUrlRepository shortUrlRepository;

    RedirectControllerIT(MockMvc mockMvc, ShortUrlRepository shortUrlRepository) {
        this.mockMvc = mockMvc;
        this.shortUrlRepository = shortUrlRepository;
    }

    @Test
    void should_redirect_when_shortCode_exists_and_not_expired() throws Exception {
        // GIVEN
        ShortUrl shortUrl = new ShortUrl(
                "https://google.com",
                "abc",
                null
        );
        shortUrlRepository.save(shortUrl);

        // WHEN
        mockMvc.perform(get("/abc"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://google.com"));

        // THEN
        ShortUrl updated = shortUrlRepository
                .findByShortCode("abc")
                .orElseThrow();

        assertEquals(1L, updated.getClickCount());
    }

    @Test
    void should_return_404_when_shortCode_not_found() throws Exception {
        mockMvc.perform(get("/not-exists")).andExpect(status().isNotFound());
    }

    @Test
    void should_redirect_401_when_shortCode_expired() throws Exception {
        // GIVEN
        ShortUrl expiredUrl = new ShortUrl(
                "https://google.com",
                "expired",
                null
        );
        shortUrlRepository.save(expiredUrl);

        // WHEN
        mockMvc.perform(get("/expired"))
                .andExpect(status().isGone());
    }

}