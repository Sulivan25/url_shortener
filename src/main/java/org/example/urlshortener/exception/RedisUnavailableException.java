package org.example.urlshortener.exception;

/**
 * Thrown (or used as a marker) when a Redis operation fails because the Redis server
 * is unreachable or rejecting connections.
 *
 * <p>Redis is treated as <em>optional</em> infrastructure in this project: a write-through
 * cache for hot click counts, not the source of truth. Code that catches this should
 * degrade gracefully (e.g., schedulers skip the run, hot-path lookups fall back to the DB)
 * rather than propagate the failure to callers.
 */
public class RedisUnavailableException extends RuntimeException {

    public RedisUnavailableException(String operation, Throwable cause) {
        super("Redis is unavailable during " + operation + ": " + cause.getMessage(), cause);
    }
}
