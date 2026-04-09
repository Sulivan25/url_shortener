package org.example.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TimeSeriesResponse {
    private String bucket;
    private long clicks;
}
