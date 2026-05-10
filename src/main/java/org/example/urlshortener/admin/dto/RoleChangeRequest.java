package org.example.urlshortener.admin.dto;

import jakarta.validation.constraints.NotNull;
import org.example.urlshortener.user.Role;

public record RoleChangeRequest(@NotNull Role role) {}
