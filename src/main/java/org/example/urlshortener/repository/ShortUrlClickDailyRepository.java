package org.example.urlshortener.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.example.urlshortener.domain.entity.ClickDaily;
import org.example.urlshortener.domain.entity.ClickHourly;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShortUrlClickDailyRepository
        extends JpaRepository<ClickDaily, Long> {

    @Modifying
    @Query("""
    INSERT INTO short_url_click_daily(short_code, day_bucket, click_count)
    VALUES (:code, :day, :count)
    ON CONFLICT (short_code, day_bucket)
    DO UPDATE SET click_count =
        short_url_click_daily.click_count + :count
    """)
    void upsert(
            @Param("code") String code,
            @Param("day") String day,
            @Param("count") long count
    );

    @Query("""
    SELECT h FROM ShortUrlClickDaily h
    WHERE h.shortCode = :code
    ORDER BY h.dayBucket DESC
    """)
    List<ClickDaily> findRecent(
            @Param("code") String code,
            Pageable pageable
    );
}
