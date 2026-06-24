package com.workerpay.config;

import com.workerpay.auth.entity.Role;
import com.workerpay.auth.entity.User;
import com.workerpay.auth.repository.RoleRepository;
import com.workerpay.auth.repository.UserRepository;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public DataInitializer(
        RoleRepository roleRepository,
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        @Value("${app.admin.username:}") String adminUsername,
        @Value("${app.admin.password:}") String adminPassword
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        Role adminRole = roleRepository.findByName("ADMIN")
            .orElseGet(() -> roleRepository.save(new Role("ADMIN")));

        roleRepository.findByName("OPERATOR")
            .orElseGet(() -> roleRepository.save(new Role("OPERATOR")));

        if (!userRepository.existsAdminUser()) {
            validateAdminEnvironment();
            User admin = new User();
            admin.setUsername(adminUsername.trim());
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setEnabled(true);
            admin.setRoles(Set.of(adminRole));
            userRepository.save(admin);
        }
    }

    private void validateAdminEnvironment() {
        if (!StringUtils.hasText(adminUsername) || !StringUtils.hasText(adminPassword)) {
            throw new IllegalStateException(
                "Faltan APP_ADMIN_USERNAME o APP_ADMIN_PASSWORD para crear el usuario administrador inicial."
            );
        }
        if (adminPassword.length() < 12) {
            throw new IllegalStateException(
                "APP_ADMIN_PASSWORD debe tener al menos 12 caracteres para crear el usuario administrador inicial."
            );
        }
    }
}
