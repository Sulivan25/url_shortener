package org.example.urlshortener.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TimeSeriesResponse {
    private String bucket;
    private long clicks;
}
