package com.reoagms.identity.role.repository;

import com.reoagms.identity.common.enums.RoleType;
import com.reoagms.identity.role.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(RoleType name);

    boolean existsByName(RoleType name);

}