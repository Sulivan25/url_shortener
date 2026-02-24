package org.example.urlshortener.controller;

import org.example.urlshortener.dto.ExtendExpirationRequest;
import org.example.urlshortener.service.UrlShortenerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/admin/short-urls")
public class ShortUrlAdminController {

    private final UrlShortenerService urlShortenerService;
    public ShortUrlAdminController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    @PostMapping("admin/{shortCode}/extend")
    public ResponseEntity<Void> extendExpiration(
            @PathVariable String shortCode,
            @RequestBody ExtendExpirationRequest request
    ) {
        urlShortenerService.execute(shortCode, request.getDays());
        return ResponseEntity.noContent().build();
    }

}
