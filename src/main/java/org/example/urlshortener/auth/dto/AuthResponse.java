package org.example.urlshortener.auth.dto;

import org.example.urlshortener.user.Role;

public record AuthResponse(
        String token,
        String username,
        Role role
) {}
