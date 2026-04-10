package org.example.urlshortener.auth;

import lombok.RequiredArgsConstructor;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        String token = jwtService.generateToken(user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }

    public AuthResponse login(LoginRequest req) {
        // Delegate to Spring's AuthenticationManager — runs the full pipeline
        // (UserDetailsService + BCrypt match) and throws BadCredentialsException on failure.
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));

        AuthUserPrincipal principal = (AuthUserPrincipal) auth.getPrincipal();
        String token = jwtService.generateToken(principal.getUsername(), principal.getRole());
        return new AuthResponse(token, principal.getUsername(), principal.getRole());
    }
}
