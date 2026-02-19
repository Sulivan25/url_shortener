package org.example.urlshortener.exception;

public class ShortUrlExpiredException extends RuntimeException{
    public ShortUrlExpiredException(String shortCode) {
        super("Short URl expired: + " + shortCode);
    }
}
