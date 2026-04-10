package org.example.urlshortener.security;

import lombok.RequiredArgsConstructor;
import org.example.urlshortener.user.User;
import org.example.urlshortener.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security's SPI for "given a username, return auth data".
 *
 * <p>Called from two places:
 * <ul>
 *   <li>{@link org.springframework.security.authentication.dao.DaoAuthenticationProvider}
 *       during {@code POST /auth/login} — to compare the supplied password against the stored hash.</li>
 *   <li>{@link JwtAuthenticationFilter} on every authenticated request — to load the user
 *       referenced by the token's subject claim.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new AuthUserPrincipal(user);
    }
}
