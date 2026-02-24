package org.example.urlshortener.scheduler;


import lombok.RequiredArgsConstructor;
import org.example.urlshortener.infrastructure.redis.RedisKeyHelper;
import org.example.urlshortener.repository.ShortUrlClickHourlyRepository;
import org.example.urlshortener.repository.ShortUrlRepository;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class HourlyClickSyncJob {

    private final StringRedisTemplate redisTemplate;
    private final ShortUrlRepository shortUrlRepository;
    private final ShortUrlClickHourlyRepository hourlyRepository;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void syncHourlyClicks() {

        ScanOptions options = ScanOptions.scanOptions()
                .match(RedisKeyHelper.hourlyPattern())
                .count(100)
                .build();

        try (Cursor<byte[]> cursor =
                     redisTemplate.getConnectionFactory()
                             .getConnection()
                             .scan(options)) {

            while (cursor.hasNext()) {
                String key = new String(cursor.next());

                String[] parts = key.split(":");
                String shortCode = parts[1];
                String hourBucket = parts[5];

                long clicks = Long.parseLong(
                        redisTemplate.opsForValue().get(key)
                );

                hourlyRepository.upsert(
                        shortCode, hourBucket, clicks
                );

                redisTemplate.delete(key);
            }
        }
    }
}
