package org.example.urlshortener.exception;

public class ShortUrlExpiredException extends RuntimeException{
    public ShortUrlExpiredException(String shortCode) {
        super("Number of days is not valid: + " + shortCode);
    }
}
