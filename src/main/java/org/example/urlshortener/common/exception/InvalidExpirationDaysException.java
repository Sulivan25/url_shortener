package org.example.urlshortener.common.exception;

public class InvalidExpirationDaysException extends RuntimeException {
    public InvalidExpirationDaysException(String shortCode, int days) {
        super("Number of days are not valid " + shortCode + " " + days);
    }
}
