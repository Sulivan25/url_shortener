package org.example.urlshortener.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.urlshortener.domain.entity.ShortUrl;
import org.example.urlshortener.infrastructure.redis.RedisKeyHelper;
import org.example.urlshortener.repository.ShortUrlRepository;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ClickCountSyncJob {

    private final StringRedisTemplate redisTemplate;
    private final ShortUrlRepository shortUrlRepository;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void sync() {

        ScanOptions options = ScanOptions.scanOptions()
                .match(RedisKeyHelper.clickCountPattern())
                .count(100)
                .build();

        try (Cursor<byte[]> cursor =
                     redisTemplate.getConnectionFactory()
                             .getConnection()
                             .scan(options)) {

            while (cursor.hasNext()) {
                String key = new String(cursor.next());

                String shortCode = RedisKeyHelper.extractShortCodeFromClickKey(key);

                String value = redisTemplate.opsForValue().get(key);
                if (value == null) {
                    redisTemplate.delete(key);
                    continue;
                }

                long clicks = Long.parseLong(value);

                ShortUrl shortUrl = shortUrlRepository
                        .findByShortCode(shortCode)
                        .orElse(null);

                if (shortUrl != null) {
                    shortUrl.addClickCount(clicks);
                    shortUrlRepository.save(shortUrl);
                }

                redisTemplate.delete(key);
            }
        }
    }
}
