package org.example.urlshortener.common.exception;

public class ShortUrlExpiredException extends RuntimeException{
    public ShortUrlExpiredException(String shortCode) {
        super("Number of days is not valid: + " + shortCode);
    }
}
