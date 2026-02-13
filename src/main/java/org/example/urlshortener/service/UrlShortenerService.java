package org.example.urlshortener.service;

import org.springframework.stereotype.Service;
import org.example.urlshortener.repository.ShortUrlRepository;
import org.example.urlshortener.domain.entity.ShortUrl;
import java.time.LocalDateTime;

@Service
public class UrlShortenerService {

    private final ShortUrlRepository shortUrlRepository;

    public UrlShortenerService(ShortUrlRepository shortUrlRepository) {
        this.shortUrlRepository = shortUrlRepository;
    }

    public ShortUrl createShortUrl(String originalUrl, Integer expireDays){
        LocalDateTime expireAt = null;

        if (expireDays != null) {
            expireAt = LocalDateTime.now().plusDays(expireDays);
        }
        ShortUrl shortUrl = new ShortUrl(originalUrl,null,expireAt);

        return shortUrlRepository.save(shortUrl);
    }

}
