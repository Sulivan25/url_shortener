package org.example.urlshortener.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.example.urlshortener.domain.entity.ClickHourly;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShortUrlClickHourlyRepository
        extends JpaRepository<ClickHourly, Long> {

    @Modifying
    @Query("""
    INSERT INTO short_url_click_hourly(short_code, hour_bucket, click_count)
    VALUES (:code, :hour, :count)
    ON CONFLICT (short_code, hour_bucket)
    DO UPDATE SET click_count =
        short_url_click_hourly.click_count + :count
    """)
    void upsert(
            @Param("code") String code,
            @Param("hour") String hour,
            @Param("count") long count
    );

    @Query("""
    SELECT h FROM ShortUrlClickHourly h
    WHERE h.shortCode = :code
    ORDER BY h.hourBucket DESC
    """)
    List<ClickHourly> findRecent(
            @Param("code") String code,
            Pageable pageable
    );
}
