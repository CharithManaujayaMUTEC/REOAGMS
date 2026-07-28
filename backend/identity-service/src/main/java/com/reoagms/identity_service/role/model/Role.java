package com.reoagms.identity.role.model;

import com.reoagms.identity.common.enums.RoleType;
import com.reoagms.identity.common.model.BaseEntity;
import com.reoagms.identity.user.model.User;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleType name;

    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "role")
    private List<User> users;

}