package org.example.urlshortener.common.exception;

public class ShortUrlNotFoundException extends RuntimeException {
    public ShortUrlNotFoundException(String shortCode) {
        super("Short URL not found: " + shortCode);
    }
}
