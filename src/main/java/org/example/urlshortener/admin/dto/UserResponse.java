package org.example.urlshortener.admin.dto;

import org.example.urlshortener.user.Role;
import org.example.urlshortener.user.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        Role role,
        LocalDateTime createdAt
) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getRole(), u.getCreatedAt());
    }
}
