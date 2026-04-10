package org.example.urlshortener.repository;

import org.example.urlshortener.domain.entity.ClickDaily;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShortUrlClickDailyRepository
        extends JpaRepository<ClickDaily, Long> {

    // nativeQuery = true because this uses raw SQL (table names, ON CONFLICT)
    // rather than JPQL (entity names). Works on Postgres natively and on H2
    // when the JDBC URL has MODE=PostgreSQL.
    @Modifying
    @Query(value = """
    INSERT INTO short_url_click_daily(short_code, day_bucket, click_count)
    VALUES (:code, :day, :count)
    ON CONFLICT (short_code, day_bucket)
    DO UPDATE SET click_count =
        short_url_click_daily.click_count + :count
    """, nativeQuery = true)
    void upsert(
            @Param("code") String code,
            @Param("day") String day,
            @Param("count") long count
    );

    // JPQL: uses the entity class name (ClickDaily), not the table name.
    @Query("""
    SELECT d FROM ClickDaily d
    WHERE d.shortCode = :code
    ORDER BY d.dayBucket DESC
    """)
    List<ClickDaily> findRecent(
            @Param("code") String code,
            Pageable pageable
    );
}
