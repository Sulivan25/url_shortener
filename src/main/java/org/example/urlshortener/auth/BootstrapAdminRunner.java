package org.example.urlshortener.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.urlshortener.user.Role;
import org.example.urlshortener.user.User;
import org.example.urlshortener.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Idempotent admin seeder. Runs once after the Spring context is fully initialized.
 *
 * <p>Without this, a brand-new database has zero users — and since admin endpoints
 * require an ADMIN role, there'd be no way to bootstrap the system. The credentials
 * come from {@code app.security.bootstrap-admin.*} in application.yml.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BootstrapAdminRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.bootstrap-admin.username}")
    private String bootstrapUsername;

    @Value("${app.security.bootstrap-admin.password}")
    private String bootstrapPassword;

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername(bootstrapUsername)) {
            return;
        }
        User admin = new User(bootstrapUsername, passwordEncoder.encode(bootstrapPassword), Role.ADMIN);
        userRepository.save(admin);
        log.atInfo()
                .addKeyValue("username", bootstrapUsername)
                .addKeyValue("role", Role.ADMIN)
                .log("bootstrap_admin_created");
    }
}
