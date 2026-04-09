package org.example.urlshortener.service;

import lombok.RequiredArgsConstructor;
import org.example.urlshortener.dto.TimeSeriesResponse;
import org.example.urlshortener.repository.ShortUrlClickDailyRepository;
import org.example.urlshortener.repository.ShortUrlClickHourlyRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final ShortUrlClickHourlyRepository hourlyRepository;
    private final ShortUrlClickDailyRepository dailyRepository;

    public List<TimeSeriesResponse> hourlyAnalytics(
            String shortCode,
            int hours) {

        return hourlyRepository
                .findRecent(
                        shortCode,
                        PageRequest.of(0, hours)
                )
                .stream()
                .map(h -> new TimeSeriesResponse(
                        h.getHourBucket(),
                        h.getClickCount()
                ))
                .toList();
    }


    public List<TimeSeriesResponse> dailyAnalytics(
            String shortCode,
            int days) {

        return dailyRepository
                .findRecent(
                        shortCode,
                        PageRequest.of(0, days)
                )
                .stream()
                .map(h -> new TimeSeriesResponse(
                        h.getDayBucket(),
                        h.getClickCount()
                ))
                .toList();
    }


}
