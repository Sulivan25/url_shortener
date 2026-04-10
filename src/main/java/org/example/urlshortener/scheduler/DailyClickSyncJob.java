package org.example.urlshortener.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.urlshortener.exception.RedisUnavailableException;
import org.example.urlshortener.infrastructure.redis.RedisKeyHelper;
import org.example.urlshortener.repository.ShortUrlClickDailyRepository;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyClickSyncJob {

    private final StringRedisTemplate redisTemplate;
    private final ShortUrlClickDailyRepository dailyRepository;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void syncDailyClicks() {

        ScanOptions options = ScanOptions.scanOptions()
                .match(RedisKeyHelper.dailyPattern())
                .count(100)
                .build();

        // Redis is optional infrastructure. Skip this run if it's unavailable.
        try (Cursor<byte[]> cursor =
                     redisTemplate.getConnectionFactory()
                             .getConnection()
                             .scan(options)) {

            while (cursor.hasNext()) {
                String key = new String(cursor.next());

                String[] parts = key.split(":");
                String shortCode = parts[1];
                String dayBucket = parts[5];

                long clicks = Long.parseLong(
                        redisTemplate.opsForValue().get(key)
                );

                dailyRepository.upsert(
                        shortCode, dayBucket, clicks
                );

                redisTemplate.delete(key);
            }
        } catch (RedisConnectionFailureException cause) {
            RedisUnavailableException ex = new RedisUnavailableException("daily click sync", cause);
            log.warn("Skipping scheduled run: {}", ex.getMessage());
        }
    }
}
