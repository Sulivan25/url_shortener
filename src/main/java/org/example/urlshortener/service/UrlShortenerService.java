package org.example.urlshortener.service;

import org.example.urlshortener.util.Base62Util;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.example.urlshortener.repository.ShortUrlRepository;
import org.example.urlshortener.domain.entity.ShortUrl;
import org.springframework.web.server.ResponseStatusException;

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

        // 1st time save to get ID
        ShortUrl shortUrl = new ShortUrl(originalUrl,null,expireAt);
        shortUrl = shortUrlRepository.save(shortUrl);

        // Generate shortCode from ID
        String shortCode = Base62Util.encode(shortUrl.getId());

        // Set ShortCode
        shortUrl.setShortCode(shortCode);

        // 2nd time save to update ShortCode
        return shortUrlRepository.save(shortUrl);
    }

    public String resolveShortCode(String shortCode){
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (shortUrl.isExpired()) {
            throw new ResponseStatusException(HttpStatus.GONE, "Short URL has expired");
        }

        shortUrl.increaseClickCount();
        shortUrlRepository.save(shortUrl);

        return shortUrl.getOriginalUrl();
    }

    public ShortUrl getOriginalUrl(String shortcode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortcode)
                .orElseThrow(() -> new RuntimeException("Short URL not found"));

        if(shortUrl.isExpired()){
            throw new RuntimeException("Short URL expired");
        }

        return shortUrl;
    }

}
