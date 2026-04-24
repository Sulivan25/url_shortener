package org.example.urlshortener.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.urlshortener.admin.dto.AdminCreateUserRequest;
import org.example.urlshortener.admin.dto.RoleChangeRequest;
import org.example.urlshortener.admin.dto.UserResponse;
import org.example.urlshortener.exception.UsernameTakenException;
import org.example.urlshortener.user.User;
import org.example.urlshortener.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only user management. Class-level {@code @PreAuthorize} pairs with the
 * {@code /admin/**} URL matcher in SecurityConfig as defense in depth.
 */
@Slf4j
@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public Page<UserResponse> listAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::from);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody AdminCreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameTakenException(request.username());
        }
        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password()),
                request.role());
        User saved = userRepository.save(user);
        log.atInfo()
                .addKeyValue("userId", saved.getId())
                .addKeyValue("username", saved.getUsername())
                .addKeyValue("role", saved.getRole())
                .log("admin_user_created");
        return UserResponse.from(saved);
    }

    @PatchMapping("/{id}/role")
    public UserResponse changeRole(@PathVariable Long id, @Valid @RequestBody RoleChangeRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User id not found: " + id));
        user.setRole(request.role());
        User saved = userRepository.save(user);
        log.atInfo()
                .addKeyValue("userId", saved.getId())
                .addKeyValue("role", saved.getRole())
                .log("admin_user_role_changed");
        return UserResponse.from(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            throw new UsernameNotFoundException("User id not found: " + id);
        }
        userRepository.deleteById(id);
        log.atInfo()
                .addKeyValue("userId", id)
                .log("admin_user_deleted");
        return ResponseEntity.noContent().build();
    }
}
