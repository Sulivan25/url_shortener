package org.example.urlshortener.controller;

import lombok.RequiredArgsConstructor;
import org.example.urlshortener.dto.TimeSeriesResponse;
import org.example.urlshortener.service.AnalyticsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/short-urls")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/{shortCode}/analytics/hourly")
    public List<TimeSeriesResponse> hourly(
            @PathVariable String shortCode,
            @RequestParam(defaultValue = "24") int hours) {

        return analyticsService
                .hourlyAnalytics(shortCode, hours);
    }
}
