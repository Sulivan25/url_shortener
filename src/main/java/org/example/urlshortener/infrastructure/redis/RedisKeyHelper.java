package org.example.urlshortener.infrastructure.redis;

public final class RedisKeyHelper {

    private static final String PREFIX = "shorturl:";

    private RedisKeyHelper() {}

    public static String redirectKey(String shortCode) {
        return PREFIX + shortCode;
    }

    public static String clickKey(String shortCode) {
        return PREFIX + shortCode + ":clicks";
    }

    public static String clickCountKey(String shortCode) {
        return "shorturl:" + shortCode + ":clicks";
    }

    public static String clickCountPattern() {
        return "shorturl:*:clicks";
    }

    public static String extractShortCodeFromClickKey(String key) {
        // shorturl:{code}:clicks
        return key.split(":")[1];
    }
}
