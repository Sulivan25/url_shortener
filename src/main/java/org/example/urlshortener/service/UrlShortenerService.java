package org.example.urlshortener.service;

import org.example.urlshortener.exception.ShortUrlExpiredException;
import org.example.urlshortener.exception.ShortUrlNotFoundException;
import org.example.urlshortener.infrastructure.redis.RedisKeyHelper;
import org.example.urlshortener.user.User;
import org.example.urlshortener.util.Base62Util;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.example.urlshortener.repository.ShortUrlRepository;
import org.example.urlshortener.domain.entity.ShortUrl;
import org.springframework.transaction.annotation.Transactional;
import static org.example.urlshortener.infrastructure.redis.RedisKeyHelper.*;

import java.time.LocalDateTime;

@Service
public class UrlShortenerService {

    private final ShortUrlRepository shortUrlRepository;
    private final StringRedisTemplate redisTemplate;


    public UrlShortenerService(ShortUrlRepository shortUrlRepository, StringRedisTemplate redisTemplate) {
        this.shortUrlRepository = shortUrlRepository;
        this.redisTemplate = redisTemplate;
    }


    public ShortUrl createShortUrl(String originalUrl, Integer expireDays) {
        return createShortUrl(originalUrl, expireDays, null);
    }

    public ShortUrl createShortUrl(String originalUrl, Integer expireDays, User owner) {
        LocalDateTime expireAt = null;

        if (expireDays != null) {
            expireAt = LocalDateTime.now().plusDays(expireDays);
        }

        // 1st time save to get ID
        ShortUrl shortUrl = new ShortUrl(originalUrl, null, expireAt, owner);
        shortUrl = shortUrlRepository.save(shortUrl);

        // Generate shortCode from ID
        String shortCode = Base62Util.encode(shortUrl.getId());

        // Set ShortCode
        shortUrl.setShortCode(shortCode);

        // 2nd time save to update ShortCode
        return shortUrlRepository.save(shortUrl);
    }

    public Page<ShortUrl> listByOwner(Long ownerId, Pageable pageable) {
        return shortUrlRepository.findByOwner_Id(ownerId, pageable);
    }

    public String getValidShortUrl(String shortCode) {

        try {
            String originalUrl = redisTemplate.opsForValue()
                    .get(redirectKey(shortCode));

            if (originalUrl != null) {
                redisTemplate.opsForValue().increment(clickKey(shortCode));
                redisTemplate.opsForValue().increment(
                        RedisKeyHelper.hourlyClickKey(shortCode)
                );
                redisTemplate.opsForValue().increment(
                        RedisKeyHelper.dailyClickKey(shortCode)
                );
                return originalUrl;
            }
        } catch (Exception redisDown) {
            // log.warn("Redis down", redisDown);
        }

        // DB fallback
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortUrlNotFoundException(shortCode));

        if (shortUrl.isExpired()) {
            throw new ShortUrlExpiredException(shortCode);
        }

        // Redis fail → update DB trực tiếp
        shortUrl.increaseClickCount();
        shortUrlRepository.save(shortUrl);

        return shortUrl.getOriginalUrl();
    }

    @Transactional
    public void execute(String shortCode, int days) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortUrlNotFoundException(shortCode));



        shortUrl.extendExpirationDays(days);
        shortUrlRepository.save(shortUrl);

    }

    @Transactional
    public void deleteByShortCode(String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortUrlNotFoundException(shortCode));
        shortUrlRepository.delete(shortUrl);
    }

    @Transactional
    public void deleteOwned(String shortCode, Long ownerId) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortUrlNotFoundException(shortCode));
        requireOwner(shortUrl, ownerId);
        shortUrlRepository.delete(shortUrl);
    }

    @Transactional
    public void extendOwned(String shortCode, int days, Long ownerId) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortUrlNotFoundException(shortCode));
        requireOwner(shortUrl, ownerId);
        shortUrl.extendExpirationDays(days);
        shortUrlRepository.save(shortUrl);
    }

    private static void requireOwner(ShortUrl shortUrl, Long ownerId) {
        if (shortUrl.getOwner() == null || !shortUrl.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("Not the owner of " + shortUrl.getShortCode());
        }
    }


}


