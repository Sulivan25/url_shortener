package org.example.urlshortener.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "short_url_click_daily",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"short_code", "day_bucket"})
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClickDaily {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_code", nullable = false)
    private String shortCode;

    @Column(name = "day_bucket", nullable = false)
    private String dayBucket;

    @Column(name = "click_count", nullable = false)
    private long clickCount;
}
