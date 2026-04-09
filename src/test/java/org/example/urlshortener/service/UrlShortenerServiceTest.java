package org.example.urlshortener.service;

import org.example.urlshortener.domain.entity.ShortUrl;
import org.example.urlshortener.exception.ShortUrlExpiredException;
import org.example.urlshortener.exception.ShortUrlNotFoundException;
import org.example.urlshortener.repository.ShortUrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

    @Mock
    private ShortUrlRepository shortUrlRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private UrlShortenerService service;

    @BeforeEach
    void setUp() {
        service = new UrlShortenerService(shortUrlRepository, redisTemplate);
    }

    @Test
    void createShortUrl_saves_and_generates_shortCode() {
        when(shortUrlRepository.save(any(ShortUrl.class)))
                .thenAnswer(invocation -> {
                    ShortUrl s = invocation.getArgument(0);
                    setId(s, 1L);
                    return s;
                });

        ShortUrl result = service.createShortUrl("https://example.com", null);

        assertNotNull(result);
        assertNotNull(result.getShortCode());
        verify(shortUrlRepository, times(2)).save(any(ShortUrl.class));
    }

    @Test
    void createShortUrl_with_expireDays_sets_expiration() {
        when(shortUrlRepository.save(any(ShortUrl.class)))
                .thenAnswer(invocation -> {
                    ShortUrl s = invocation.getArgument(0);
                    setId(s, 2L);
                    return s;
                });

        ShortUrl result = service.createShortUrl("https://example.com", 7);

        assertNotNull(result.getExpireAt());
        assertTrue(result.getExpireAt().isAfter(LocalDateTime.now().plusDays(6)));
    }

    @Test
    void createShortUrl_without_expireDays_no_expiration() {
        when(shortUrlRepository.save(any(ShortUrl.class)))
                .thenAnswer(invocation -> {
                    ShortUrl s = invocation.getArgument(0);
                    setId(s, 3L);
                    return s;
                });

        ShortUrl result = service.createShortUrl("https://example.com", null);

        assertNull(result.getExpireAt());
    }

    @Test
    void getValidShortUrl_from_redis_cache_hit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("shorturl:abc")).thenReturn("https://example.com");

        String result = service.getValidShortUrl("abc");

        assertEquals("https://example.com", result);
        verify(valueOperations).increment("shorturl:abc:clicks");
    }

    @Test
    void getValidShortUrl_redis_miss_falls_back_to_db() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("shorturl:abc")).thenReturn(null);

        ShortUrl shortUrl = new ShortUrl("https://example.com", "abc", null);
        when(shortUrlRepository.findByShortCode("abc")).thenReturn(Optional.of(shortUrl));

        String result = service.getValidShortUrl("abc");

        assertEquals("https://example.com", result);
        assertEquals(1L, shortUrl.getClickCount());
        verify(shortUrlRepository).save(shortUrl);
    }

    @Test
    void getValidShortUrl_not_found_throws() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(shortUrlRepository.findByShortCode("missing")).thenReturn(Optional.empty());

        assertThrows(ShortUrlNotFoundException.class,
                () -> service.getValidShortUrl("missing"));
    }

    @Test
    void getValidShortUrl_expired_throws() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        ShortUrl expired = new ShortUrl("https://example.com", "exp",
                LocalDateTime.now().minusDays(1));
        when(shortUrlRepository.findByShortCode("exp")).thenReturn(Optional.of(expired));

        assertThrows(ShortUrlExpiredException.class,
                () -> service.getValidShortUrl("exp"));
    }

    @Test
    void execute_extends_expiration() {
        ShortUrl shortUrl = new ShortUrl("https://example.com", "abc",
                LocalDateTime.now().plusDays(3));
        when(shortUrlRepository.findByShortCode("abc")).thenReturn(Optional.of(shortUrl));

        service.execute("abc", 5);

        assertTrue(shortUrl.getExpireAt().isAfter(LocalDateTime.now().plusDays(7)));
        verify(shortUrlRepository).save(shortUrl);
    }

    @Test
    void execute_not_found_throws() {
        when(shortUrlRepository.findByShortCode("missing")).thenReturn(Optional.empty());

        assertThrows(ShortUrlNotFoundException.class,
                () -> service.execute("missing", 5));
    }

    private void setId(ShortUrl shortUrl, Long id) {
        try {
            Field idField = ShortUrl.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(shortUrl, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
