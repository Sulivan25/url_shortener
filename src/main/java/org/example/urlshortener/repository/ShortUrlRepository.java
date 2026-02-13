package org.example.urlshortener.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.example.urlshortener.domain.entity.ShortUrl;
import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByShortCode(String shortCode);

}

