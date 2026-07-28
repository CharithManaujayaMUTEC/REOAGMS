package com.reoagms.identity_service.role.service;

import com.reoagms.identity_service.common.enums.RoleType;
import com.reoagms.identity_service.role.model.Role;
import com.reoagms.identity_service.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public Role getRole(RoleType roleType) {
        return roleRepository.findByName(roleType)
                .orElseThrow(() ->
                        new RuntimeException("Role not found: " + roleType));
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}