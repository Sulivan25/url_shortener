package org.example.urlshortener.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.example.urlshortener.domain.entity.ShortUrl;
import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByShortCode(String shortCode);

    // Spring Data parses "Owner_Id" as "the id field of the owner association".
    Page<ShortUrl> findByOwner_Id(Long ownerId, Pageable pageable);

}

