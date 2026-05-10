package org.example.urlshortener.admin;

import lombok.RequiredArgsConstructor;
import org.example.urlshortener.dto.ExtendExpirationRequest;
import org.example.urlshortener.dto.ShortUrlResponse;
import org.example.urlshortener.dto.TimeSeriesResponse;
import org.example.urlshortener.repository.ShortUrlRepository;
import org.example.urlshortener.service.AnalyticsService;
import org.example.urlshortener.service.UrlShortenerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin-only URL management. Replaces the old {@code ShortUrlAdminController}, which had
 * no auth checks at all.
 */
@RestController
@RequestMapping("/admin/short-urls")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUrlController {

    private final ShortUrlRepository shortUrlRepository;
    private final UrlShortenerService urlShortenerService;
    private final AnalyticsService analyticsService;

    @GetMapping
    public Page<ShortUrlResponse> listAll(Pageable pageable) {
        return shortUrlRepository.findAll(pageable).map(ShortUrlResponse::from);
    }

    @PostMapping("/{shortCode}/extend")
    public ResponseEntity<Void> extendAny(
            @PathVariable String shortCode,
            @RequestBody ExtendExpirationRequest request
    ) {
        urlShortenerService.execute(shortCode, request.getDays());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> deleteAny(@PathVariable String shortCode) {
        urlShortenerService.deleteByShortCode(shortCode);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{shortCode}/analytics/hourly")
    public List<TimeSeriesResponse> analyticsAny(
            @PathVariable String shortCode,
            @RequestParam(defaultValue = "24") int hours
    ) {
        return analyticsService.hourlyAnalytics(shortCode, hours);
    }
}
