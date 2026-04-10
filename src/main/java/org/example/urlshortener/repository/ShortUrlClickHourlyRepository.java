package org.example.urlshortener.repository;

import org.example.urlshortener.domain.entity.ClickHourly;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShortUrlClickHourlyRepository
        extends JpaRepository<ClickHourly, Long> {

    // nativeQuery = true because this uses raw SQL (table names, ON CONFLICT)
    // rather than JPQL (entity names). Works on Postgres natively and on H2
    // when the JDBC URL has MODE=PostgreSQL.
    @Modifying
    @Query(value = """
    INSERT INTO short_url_click_hourly(short_code, hour_bucket, click_count)
    VALUES (:code, :hour, :count)
    ON CONFLICT (short_code, hour_bucket)
    DO UPDATE SET click_count =
        short_url_click_hourly.click_count + :count
    """, nativeQuery = true)
    void upsert(
            @Param("code") String code,
            @Param("hour") String hour,
            @Param("count") long count
    );

    // JPQL: uses the entity class name (ClickHourly), not the table name.
    @Query("""
    SELECT h FROM ClickHourly h
    WHERE h.shortCode = :code
    ORDER BY h.hourBucket DESC
    """)
    List<ClickHourly> findRecent(
            @Param("code") String code,
            Pageable pageable
    );
}
