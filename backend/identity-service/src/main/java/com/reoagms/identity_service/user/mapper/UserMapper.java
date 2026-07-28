package com.reoagms.identity_service.user.mapper;

import com.reoagms.identity_service.user.dto.UserResponse;
import com.reoagms.identity_service.user.model.User;

public class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole().getName().name())
                .build();

    }

}