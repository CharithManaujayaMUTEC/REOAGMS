package com.reoagms.identity_service.config;

import com.reoagms.identity_service.common.enums.RoleType;
import com.reoagms.identity_service.role.model.Role;
import com.reoagms.identity_service.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleDataInitializer implements CommandLineRunner {

    private final RoleRepository repository;

    @Override
    public void run(String... args) {

        for (RoleType type : RoleType.values()) {

            if (!repository.existsByName(type)) {

                Role role = new Role();

                role.setName(type);

                role.setDescription(type.name());

                repository.save(role);

            }

        }

    }

}