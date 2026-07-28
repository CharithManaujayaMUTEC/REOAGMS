package com.reoagms.identity_service.role.service;

import com.reoagms.identity_service.common.enums.RoleType;
import com.reoagms.identity_service.role.model.Role;

import java.util.List;

public interface RoleService {

    Role getRole(RoleType roleType);

    List<Role> getAllRoles();

}