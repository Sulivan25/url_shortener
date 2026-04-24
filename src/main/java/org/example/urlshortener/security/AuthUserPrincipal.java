package org.example.urlshortener.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.urlshortener.user.Role;
import org.example.urlshortener.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adapter that wraps our domain {@link User} as a Spring Security {@link UserDetails}.
 *
 * <p>Spring Security only knows about UserDetails — it has no idea our app stores users
 * in a JPA entity. This class is the bridge: the {@link CustomUserDetailsService} loads
 * a User from the DB, wraps it in this principal, and Spring Security uses it from then on.
 *
 * <p>Note the {@code "ROLE_"} prefix on the authority. Spring Security's
 * {@code hasRole('ADMIN')} SpEL expression implicitly prepends {@code ROLE_} to its argument,
 * so the stored authority must be {@code "ROLE_ADMIN"} for the check to match.
 */
@RequiredArgsConstructor
public class AuthUserPrincipal implements UserDetails {

    @Getter
    private final User user;

    public Long getUserId() {
        return user.getId();
    }

    public Role getRole() {
        return user.getRole();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}
