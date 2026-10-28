package com.reoagms.identity_service.config;

import com.reoagms.identity_service.common.enums.RoleType;
import com.reoagms.identity_service.role.repository.RoleRepository;
import com.reoagms.identity_service.user.model.User;
import com.reoagms.identity_service.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates one account per role so every persona can be demonstrated. Disable with REOAGMS_SEED_DEMO_USERS=false. */
@Slf4j
@Order(2)
@Component
@RequiredArgsConstructor
public class DemoUserInitializer implements CommandLineRunner {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;
    @Value("${reoagms.seed.demo-users:true}")
    private boolean enabled;
    @Value("${reoagms.seed.demo-password:Reoagms@2026}")
    private String password;

    @Override
    public void run(String... args) {
        if (!enabled) return;
        seed("admin@reoagms.com", "System", "Administrator", RoleType.ADMIN);
        seed("manager@reoagms.com", "Olivia", "Manager", RoleType.OPERATIONS_MANAGER);
        seed("operator@reoagms.com", "Peter", "Operator", RoleType.PLANT_OPERATOR);
        seed("engineer@reoagms.com", "Mia", "Engineer", RoleType.MAINTENANCE_ENGINEER);
        seed("executive@reoagms.com", "Edward", "Executive", RoleType.EXECUTIVE_MANAGER);
    }

    private void seed(String email, String first, String last, RoleType type) {
        if (users.existsByEmail(email)) return;
        roles.findByName(type).ifPresent(role -> {
            users.save(User.builder().firstName(first).lastName(last).email(email)
                    .password(encoder.encode(password)).role(role).enabled(true).accountNonLocked(true).build());
            log.info("Seeded demo user {} ({})", email, type);
        });
    }
}
