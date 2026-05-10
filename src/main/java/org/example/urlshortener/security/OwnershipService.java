package org.example.urlshortener.security;

import lombok.RequiredArgsConstructor;
import org.example.urlshortener.repository.ShortUrlRepository;
import org.springframework.stereotype.Component;

/**
 * Bean used inside {@code @PreAuthorize} SpEL expressions to enforce per-URL ownership.
 *
 * <p>Usage:
 * <pre>
 *   &#64;PreAuthorize("hasRole('ADMIN') or @ownership.isOwner(#shortCode, principal)")
 * </pre>
 *
 * <p>SpEL syntax:
 * <ul>
 *   <li>{@code @ownership} — references this bean by name (note the explicit name on
 *       {@code @Component("ownership")} below).</li>
 *   <li>{@code principal} — built-in variable = {@code Authentication.getPrincipal()}.</li>
 *   <li>{@code #shortCode} — references the controller method argument by name.</li>
 *   <li>{@code or} short-circuits, so admins skip the DB lookup entirely.</li>
 * </ul>
 */
@Component("ownership")
@RequiredArgsConstructor
public class OwnershipService {

    private final ShortUrlRepository shortUrlRepository;

    public boolean isOwner(String shortCode, Object principal) {
        if (!(principal instanceof AuthUserPrincipal authPrincipal)) {
            return false;
        }
        return shortUrlRepository.findByShortCode(shortCode)
                .map(s -> s.getOwner() != null
                        && s.getOwner().getId().equals(authPrincipal.getUserId()))
                .orElse(false);
    }
}
