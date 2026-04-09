package org.example.urlshortener.service;

import org.example.urlshortener.domain.entity.ClickDaily;
import org.example.urlshortener.domain.entity.ClickHourly;
import org.example.urlshortener.dto.TimeSeriesResponse;
import org.example.urlshortener.repository.ShortUrlClickDailyRepository;
import org.example.urlshortener.repository.ShortUrlClickHourlyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private ShortUrlClickHourlyRepository hourlyRepository;

    @Mock
    private ShortUrlClickDailyRepository dailyRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void hourlyAnalytics_returns_mapped_responses() {
        ClickHourly h1 = new ClickHourly(1L, "abc", "20260409-14", 10);
        ClickHourly h2 = new ClickHourly(2L, "abc", "20260409-13", 5);
        when(hourlyRepository.findRecent("abc", PageRequest.of(0, 24)))
                .thenReturn(List.of(h1, h2));

        List<TimeSeriesResponse> result = analyticsService.hourlyAnalytics("abc", 24);

        assertEquals(2, result.size());
        assertEquals("20260409-14", result.get(0).getBucket());
        assertEquals(10L, result.get(0).getClicks());
        assertEquals("20260409-13", result.get(1).getBucket());
        assertEquals(5L, result.get(1).getClicks());
    }

    @Test
    void hourlyAnalytics_empty_returns_empty_list() {
        when(hourlyRepository.findRecent("abc", PageRequest.of(0, 24)))
                .thenReturn(Collections.emptyList());

        List<TimeSeriesResponse> result = analyticsService.hourlyAnalytics("abc", 24);

        assertTrue(result.isEmpty());
    }

    @Test
    void dailyAnalytics_returns_mapped_responses() {
        ClickDaily d1 = new ClickDaily(1L, "abc", "20260409", 100);
        ClickDaily d2 = new ClickDaily(2L, "abc", "20260408", 80);
        when(dailyRepository.findRecent("abc", PageRequest.of(0, 7)))
                .thenReturn(List.of(d1, d2));

        List<TimeSeriesResponse> result = analyticsService.dailyAnalytics("abc", 7);

        assertEquals(2, result.size());
        assertEquals("20260409", result.get(0).getBucket());
        assertEquals(100L, result.get(0).getClicks());
    }

    @Test
    void dailyAnalytics_empty_returns_empty_list() {
        when(dailyRepository.findRecent("abc", PageRequest.of(0, 7)))
                .thenReturn(Collections.emptyList());

        List<TimeSeriesResponse> result = analyticsService.dailyAnalytics("abc", 7);

        assertTrue(result.isEmpty());
    }
}
