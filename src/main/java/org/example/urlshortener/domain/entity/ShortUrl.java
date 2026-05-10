package org.example.urlshortener.domain.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.urlshortener.exception.InvalidExpirationDaysException;
import org.example.urlshortener.user.User;


import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "short_urls")
@Getter @NoArgsConstructor
public class ShortUrl implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "original_url", nullable = false, length = 2048) // 2048 standard length for SEO
    private String originalUrl;

    @Setter
    @Column(name = "short_code", nullable = false, unique = true, length = 16)
    private String shortCode;

    @Column(name = "created_at",nullable = false)
    private LocalDateTime createdAt;

    @Setter
    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    @Column(name = "click_count", nullable = false)
    private long clickCount;

    // Nullable on purpose: existing rows (created before auth was added) have no owner.
    // Service layer treats orphans as admin-only — regular USERs never see them.
    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "owner_id")
    private User owner;


    public ShortUrl(String originalUrl, String shortCode, LocalDateTime expireAt){

        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.createdAt = LocalDateTime.now();
        this.expireAt = expireAt;
        this.clickCount = 0L;

    }

    public ShortUrl(String originalUrl, String shortCode, LocalDateTime expireAt, User owner){
        this(originalUrl, shortCode, expireAt);
        this.owner = owner;
    }

    public void increaseClickCount() {
        this.clickCount++;
    }

    public void addClickCount(long delta) {
        this.clickCount += delta;
    }

    public boolean isExpired(){
        return expireAt != null && LocalDateTime.now().isAfter(expireAt);
    }

    public void extendExpirationDays(int days) {

        if (days <= 0) {
            throw new InvalidExpirationDaysException(shortCode,days);
        }

        expireAt = (expireAt == null ? LocalDateTime.now() : expireAt).plusDays(days);
    }
}
