package org.example.urlshortener.domain.entity;

import org.example.urlshortener.exception.InvalidExpirationDaysException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ShortUrlTest {

    @Test
    void new_shorturl_has_zero_clicks() {
        ShortUrl url = new ShortUrl("https://example.com", "abc", null);
        assertEquals(0L, url.getClickCount());
    }

    @Test
    void increaseClickCount_increments_by_one() {
        ShortUrl url = new ShortUrl("https://example.com", "abc", null);
        url.increaseClickCount();
        assertEquals(1L, url.getClickCount());
    }

    @Test
    void addClickCount_adds_delta() {
        ShortUrl url = new ShortUrl("https://example.com", "abc", null);
        url.addClickCount(5);
        assertEquals(5L, url.getClickCount());
    }

    @Test
    void isExpired_returns_false_when_null() {
        ShortUrl url = new ShortUrl("https://example.com", "abc", null);
        assertFalse(url.isExpired());
    }

    @Test
    void isExpired_returns_false_when_future() {
        ShortUrl url = new ShortUrl("https://example.com", "abc",
                LocalDateTime.now().plusDays(7));
        assertFalse(url.isExpired());
    }

    @Test
    void isExpired_returns_true_when_past() {
        ShortUrl url = new ShortUrl("https://example.com", "abc",
                LocalDateTime.now().minusDays(1));
        assertTrue(url.isExpired());
    }

    @Test
    void extendExpirationDays_positive_days() {
        LocalDateTime expireAt = LocalDateTime.now().plusDays(3);
        ShortUrl url = new ShortUrl("https://example.com", "abc", expireAt);
        url.extendExpirationDays(5);
        assertTrue(url.getExpireAt().isAfter(expireAt.plusDays(4)));
    }

    @Test
    void extendExpirationDays_zero_throws() {
        ShortUrl url = new ShortUrl("https://example.com", "abc", LocalDateTime.now());
        assertThrows(InvalidExpirationDaysException.class,
                () -> url.extendExpirationDays(0));
    }

    @Test
    void extendExpirationDays_negative_throws() {
        ShortUrl url = new ShortUrl("https://example.com", "abc", LocalDateTime.now());
        assertThrows(InvalidExpirationDaysException.class,
                () -> url.extendExpirationDays(-1));
    }

    @Test
    void extendExpirationDays_null_expireAt_uses_now() {
        ShortUrl url = new ShortUrl("https://example.com", "abc", null);
        url.extendExpirationDays(5);
        assertNotNull(url.getExpireAt());
        assertTrue(url.getExpireAt().isAfter(LocalDateTime.now().plusDays(4)));
    }
}
