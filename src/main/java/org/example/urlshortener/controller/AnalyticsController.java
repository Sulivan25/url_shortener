package org.example.urlshortener.controller;

import lombok.RequiredArgsConstructor;
import org.example.urlshortener.dto.TimeSeriesResponse;
import org.example.urlshortener.service.AnalyticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/short-urls")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PreAuthorize("hasRole('ADMIN') or @ownership.isOwner(#shortCode, principal)")
    @GetMapping("/{shortCode}/analytics/hourly")
    public List<TimeSeriesResponse> hourly(
            @PathVariable String shortCode,
            @RequestParam(defaultValue = "24") int hours) {

        return analyticsService
                .hourlyAnalytics(shortCode, hours);
    }
}
