package org.example.urlshortener.domain.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;


import java.time.LocalDateTime;

@Entity
@Table(name = "short_urls")
public class ShortUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    public String getOriginalUrl() {
        return originalUrl;
    }

    public Long getClickCount() {
        return clickCount;
    }
    public String getShortCode() {
        return shortCode;
    }
    public Long getId() {
        return id;
    }


    @Column(name = "original_url", nullable = false, length = 2048) // 2048 standard length for SEO
    private final String originalUrl;

    @Column(name = "short_code", nullable = false, unique = true, length = 16)
    private String shortCode;

    @Column(name = "created_at",nullable = false)
    private final LocalDateTime createdAt;

    @Column(name = "expire_at")
    private LocalDateTime expireAt;



    @Column(name = "click_count", nullable = false)
    private long clickCount;


    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }



    public ShortUrl(String originalUrl, String shortCode, LocalDateTime expireAt){

        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.createdAt = LocalDateTime.now();
        this.expireAt = expireAt;
        this.clickCount = 0L;

    }

    public void increaseClickCount(){
        this.clickCount++;
    }

    public boolean isExpired(){
        return expireAt != null && LocalDateTime.now().isAfter(expireAt);
    }
}
