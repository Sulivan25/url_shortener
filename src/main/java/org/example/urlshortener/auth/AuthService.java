package org.example.urlshortener.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.urlshortener.auth.dto.AuthResponse;
import org.example.urlshortener.auth.dto.LoginRequest;
import org.example.urlshortener.auth.dto.RegisterRequest;
import org.example.urlshortener.exception.UsernameTakenException;
import org.example.urlshortener.security.AuthUserPrincipal;
import org.example.urlshortener.security.JwtService;
import org.example.urlshortener.user.Role;
import org.example.urlshortener.user.User;
import org.example.urlshortener.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new UsernameTakenException(req.username());
        }
        User user = new User(req.username(), passwordEncoder.encode(req.password()), Role.USER);
        userRepository.save(user);
        log.atInfo()
                .addKeyValue("username", user.getUsername())
                .addKeyValue("role", user.getRole())
                .log("user_registered");
        String token = jwtService.generateToken(user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }

    public AuthResponse login(LoginRequest req) {
        // Delegate to Spring's AuthenticationManager — runs the full pipeline
        // (UserDetailsService + BCrypt match) and throws BadCredentialsException on failure.
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        } catch (AuthenticationException ex) {
            log.atWarn()
                    .addKeyValue("username", req.username())
                    .addKeyValue("reason", ex.getClass().getSimpleName())
                    .log("user_login_failed");
            throw ex;
        }

        AuthUserPrincipal principal = (AuthUserPrincipal) auth.getPrincipal();
        String token = jwtService.generateToken(principal.getUsername(), principal.getRole());
        log.atInfo()
                .addKeyValue("username", principal.getUsername())
                .addKeyValue("role", principal.getRole())
                .log("user_login_success");
        return new AuthResponse(token, principal.getUsername(), principal.getRole());
    }
}
