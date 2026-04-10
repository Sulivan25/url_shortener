package org.example.urlshortener.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateShortUrlRequest(
        @NotBlank @Size(max = 2048) String originalUrl,
        @Positive @Max(3650) Integer expireDays
) {}
