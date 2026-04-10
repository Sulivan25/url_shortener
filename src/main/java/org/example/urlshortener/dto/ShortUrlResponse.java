package org.example.urlshortener.dto;

import org.example.urlshortener.domain.entity.ShortUrl;

import java.time.LocalDateTime;

public record ShortUrlResponse(
        String shortCode,
        String originalUrl,
        LocalDateTime createdAt,
        LocalDateTime expireAt,
        long clickCount,
        String owner
) {
    public static ShortUrlResponse from(ShortUrl s) {
        return new ShortUrlResponse(
                s.getShortCode(),
                s.getOriginalUrl(),
                s.getCreatedAt(),
                s.getExpireAt(),
                s.getClickCount(),
                s.getOwner() == null ? null : s.getOwner().getUsername()
        );
    }
}
