package org.example.urlshortener.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "short_url_click_hourly",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"short_code", "hour_bucket"})
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClickHourly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_code", nullable = false)
    private String shortCode;

    @Column(name = "hour_bucket", nullable = false)
    private String hourBucket;

    @Column(name = "click_count", nullable = false)
    private long clickCount;
}
