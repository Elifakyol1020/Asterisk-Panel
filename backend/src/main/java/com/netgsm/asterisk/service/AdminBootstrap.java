package com.netgsm.asterisk.service;
import com.netgsm.asterisk.config.BootstrapProperties;
import com.netgsm.asterisk.enums.Role;
import com.netgsm.asterisk.entity.User;
import com.netgsm.asterisk.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component @ConditionalOnProperty(name = "app.bootstrap.enabled", havingValue = "true") @RequiredArgsConstructor @Slf4j
public class AdminBootstrap implements CommandLineRunner {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final BootstrapProperties properties;
    @Override @Transactional
    public void run(String... args) {
        if (users.existsByRole(Role.SUPER_ADMIN)) return;
        if (properties.username() == null || !properties.username().matches("[a-zA-Z0-9_.-]{3,80}")
                || properties.email() == null || !properties.email().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")
                || properties.email().length() > 254 || properties.password() == null || properties.password().length() < 12
                || properties.password().getBytes(StandardCharsets.UTF_8).length > 72)
            throw new IllegalStateException("Configure valid app.bootstrap username, email and password (12+ characters, at most 72 UTF-8 bytes)");
        User user = new User(); user.setUsername(properties.username().toLowerCase(Locale.ROOT));
        user.setEmail(properties.email().toLowerCase(Locale.ROOT)); user.setPasswordHash(passwords.encode(properties.password()));
        user.setRole(Role.SUPER_ADMIN); user.setEnabled(true); users.saveAndFlush(user);
        log.info("Initial super admin created id={}", user.getId());
    }
}
