package org.example.urlshortener.controller;

import org.example.urlshortener.domain.entity.ShortUrl;
import org.example.urlshortener.service.UrlShortenerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;


@RestController
public class RedirectController {

    public RedirectController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }
    private final UrlShortenerService urlShortenerService;



    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String shortUrl = urlShortenerService.getValidShortUrl(shortCode);

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(shortUrl)).build();
    }

}
