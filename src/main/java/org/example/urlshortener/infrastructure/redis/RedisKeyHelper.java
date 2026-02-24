package org.example.urlshortener.infrastructure.redis;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class RedisKeyHelper {

    private static final String PREFIX = "shorturl:";

    private static final DateTimeFormatter HOUR_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HH");

    private static final DateTimeFormatter DAY_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private RedisKeyHelper() {}

    public static String redirectKey(String shortCode) {
        return PREFIX + shortCode;
    }

    public static String clickKey(String shortCode) {
        return PREFIX + shortCode + ":clicks";
    }

    public static String clickCountKey(String shortCode) {
        return PREFIX + shortCode + ":clicks";
    }

    public static String clickCountPattern() {
        return PREFIX + "*:clicks";
    }

    public static String extractShortCodeFromClickKey(String key) {
        // shorturl:{code}:clicks
        return key.split(":")[1];
    }


    //Time-series
    public static String hourlyClickKey(String shortCode) {
        return PREFIX + shortCode + ":clicks:hour:" +
                LocalDateTime.now().format(HOUR_FMT);
    }

    public static String dailyClickKey(String shortCode) {
        return PREFIX + shortCode + ":clicks:day:" +
                LocalDate.now().format(DAY_FMT);
    }

    public static String hourlyPattern() {
        return PREFIX + "*:clicks:hour:*";
    }

    public static String dailyPattern() {
        return PREFIX + "*:clicks:day:*";
    }


}
