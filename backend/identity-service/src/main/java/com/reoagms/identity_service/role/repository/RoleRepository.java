package com.reoagms.identity_service.role.repository;

import com.reoagms.identity_service.common.enums.RoleType;
import com.reoagms.identity_service.role.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(RoleType name);

    boolean existsByName(RoleType name);

}