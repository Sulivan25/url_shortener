package org.example.urlshortener.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.urlshortener.domain.entity.ShortUrl;
import org.example.urlshortener.dto.CreateShortUrlRequest;
import org.example.urlshortener.dto.ExtendExpirationRequest;
import org.example.urlshortener.dto.ShortUrlResponse;
import org.example.urlshortener.security.AuthUserPrincipal;
import org.example.urlshortener.service.UrlShortenerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * User-facing CRUD on the caller's own short URLs.
 *
 * <p>Authorization model:
 * <ul>
 *   <li>{@code POST /} and {@code GET /me} only require authentication — implicit ownership
 *       (we use the principal's id directly).</li>
 *   <li>{@code extend} and {@code delete} use {@code @PreAuthorize} with the
 *       {@link org.example.urlshortener.security.OwnershipService} bean — admins bypass the
 *       check, regular users must own the URL.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/short-urls")
@RequiredArgsConstructor
public class ShortUrlController {

    private final UrlShortenerService urlShortenerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShortUrlResponse create(
            @Valid @RequestBody CreateShortUrlRequest request,
            @AuthenticationPrincipal AuthUserPrincipal me
    ) {
        ShortUrl saved = urlShortenerService.createShortUrl(
                request.originalUrl(), request.expireDays(), me.getUser());
        return ShortUrlResponse.from(saved);
    }

    @GetMapping("/me")
    public Page<ShortUrlResponse> listMine(
            @AuthenticationPrincipal AuthUserPrincipal me,
            Pageable pageable
    ) {
        return urlShortenerService.listByOwner(me.getUserId(), pageable)
                .map(ShortUrlResponse::from);
    }

    @PreAuthorize("hasRole('ADMIN') or @ownership.isOwner(#shortCode, principal)")
    @PostMapping("/{shortCode}/extend")
    public ResponseEntity<Void> extend(
            @PathVariable String shortCode,
            @RequestBody ExtendExpirationRequest request
    ) {
        urlShortenerService.execute(shortCode, request.getDays());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN') or @ownership.isOwner(#shortCode, principal)")
    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> delete(@PathVariable String shortCode) {
        // @PreAuthorize already verified admin-or-owner, so we can delete unconditionally.
        urlShortenerService.deleteByShortCode(shortCode);
        return ResponseEntity.noContent().build();
    }
}
