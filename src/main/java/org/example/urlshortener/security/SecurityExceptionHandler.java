package org.example.urlshortener.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/**
 * JSON 401/403 responses for failures that happen inside the security filter chain.
 *
 * <p>These exceptions are thrown <em>before</em> Spring MVC dispatches to a controller, so
 * {@code @RestControllerAdvice} never sees them. We must register handlers directly on the
 * filter chain (see {@link SecurityConfig}'s exceptionHandling block).
 */
public class SecurityExceptionHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Component
    public static class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

        @Override
        public void commence(HttpServletRequest request,
                             HttpServletResponse response,
                             AuthenticationException ex) throws IOException {
            writeJson(response, HttpStatus.UNAUTHORIZED, "Authentication required", request);
        }
    }

    @Component
    public static class RestAccessDeniedHandler implements AccessDeniedHandler {

        @Override
        public void handle(HttpServletRequest request,
                           HttpServletResponse response,
                           AccessDeniedException ex) throws IOException {
            writeJson(response, HttpStatus.FORBIDDEN, "Access denied", request);
        }
    }

    private static void writeJson(HttpServletResponse response,
                                  HttpStatus status,
                                  String message,
                                  HttpServletRequest request) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "path", request.getRequestURI()
        );
        MAPPER.writeValue(response.getOutputStream(), body);
    }
}
