package org.example.urlshortener.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Central Spring Security configuration.
 *
 * <p>Annotations:
 * <ul>
 *   <li>{@code @Configuration} — bean factory; {@code @Bean} methods produce singletons.</li>
 *   <li>{@code @EnableWebSecurity} — activates Spring Security's web integration.</li>
 *   <li>{@code @EnableMethodSecurity} — turns on {@code @PreAuthorize}/{@code @PostAuthorize}.
 *       <strong>Without this, method-level annotations are silently ignored.</strong></li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityExceptionHandler.RestAuthenticationEntryPoint authenticationEntryPoint;
    private final SecurityExceptionHandler.RestAccessDeniedHandler accessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes the {@link AuthenticationManager} as a bean so {@code AuthService} can call it
     * during login. The manager internally uses {@link CustomUserDetailsService} +
     * {@link PasswordEncoder} to verify credentials.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // CSRF protection is for cookie-authenticated browser apps. Stateless JWT
                // APIs aren't vulnerable to CSRF, so disable it.
                .csrf(csrf -> csrf.disable())

                // Don't create HTTP sessions. Each request must carry its own JWT;
                // the filter re-populates the SecurityContext on every request.
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Public auth endpoints
                        .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login").permitAll()

                        // Public redirect: only paths that look like a real short code (alphanumeric, 1-16 chars).
                        // /admin, /auth, /users etc. fall through to anyRequest().authenticated().
                        .requestMatchers(HttpMethod.GET, "/{shortCode:[A-Za-z0-9]{1,16}}").permitAll()

                        // H2 console (dev only)
                        .requestMatchers("/h2-console/**").permitAll()

                        // Defense in depth: matcher + class-level @PreAuthorize on admin controllers.
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated())

                // H2 console renders inside an iframe; default header policy blocks iframes.
                .headers(h -> h.frameOptions(f -> f.disable()))

                // JSON 401/403 instead of Spring's default HTML page.
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))

                // Insert our JWT filter before Spring's built-in form-login filter.
                // UsernamePasswordAuthenticationFilter is the conventional anchor position.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }
}
