package org.example.urlshortener.infrastructure.redis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RedisKeyHelperTest {

    @Test
    void redirectKey_format() {
        assertEquals("shorturl:abc", RedisKeyHelper.redirectKey("abc"));
    }

    @Test
    void clickKey_format() {
        assertEquals("shorturl:abc:clicks", RedisKeyHelper.clickKey("abc"));
    }

    @Test
    void clickCountPattern_format() {
        assertEquals("shorturl:*:clicks", RedisKeyHelper.clickCountPattern());
    }

    @Test
    void extractShortCodeFromClickKey() {
        assertEquals("abc", RedisKeyHelper.extractShortCodeFromClickKey("shorturl:abc:clicks"));
    }

    @Test
    void hourlyPattern_format() {
        assertEquals("shorturl:*:clicks:hour:*", RedisKeyHelper.hourlyPattern());
    }

    @Test
    void dailyPattern_format() {
        assertEquals("shorturl:*:clicks:day:*", RedisKeyHelper.dailyPattern());
    }
}
