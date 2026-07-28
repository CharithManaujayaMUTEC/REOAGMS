package com.reoagms.identity.role.service;

import com.reoagms.identity.common.enums.RoleType;
import com.reoagms.identity.role.model.Role;

import java.util.List;

public interface RoleService {

    Role getRole(RoleType roleType);

    List<Role> getAllRoles();

}