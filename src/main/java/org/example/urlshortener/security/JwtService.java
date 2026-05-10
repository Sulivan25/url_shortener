package org.example.urlshortener.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.urlshortener.user.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.function.Function;

/**
 * Generates and validates JSON Web Tokens.
 *
 * <p>Token shape:
 * <pre>
 *   header   : { "alg": "HS256", "typ": "JWT" }
 *   payload  : { "sub": username, "role": "USER"|"ADMIN", "iss": ..., "iat": ..., "exp": ... }
 *   signature: HMAC-SHA256(base64url(header) + "." + base64url(payload), secret)
 * </pre>
 *
 * <p>The role is stored as a claim so authorization checks don't have to hit the DB
 * on every request. Trade-off: a revoked role keeps working until the token expires.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long ttlMinutes;
    private final String issuer;

    public JwtService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.access-token-ttl-minutes}") long ttlMinutes,
            @Value("${app.security.jwt.issuer}") String issuer
    ) {
        // Use the raw bytes of the secret string. The yml default is base64-looking but
        // jjwt only requires the byte length to be >= 32 for HS256, which it is.
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlMinutes = ttlMinutes;
        this.issuer = issuer;
    }

    public String generateToken(String username, Role role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttlMinutes * 60);
        return Jwts.builder()
                .subject(username)
                .claim("role", role.name())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isValid(String token, UserDetails user) {
        try {
            String username = extractUsername(token);
            return username.equals(user.getUsername()) && !isExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }
}
