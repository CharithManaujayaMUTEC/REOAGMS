package com.reoagms.identity_service.user.service;

import com.reoagms.identity_service.user.dto.CreateUserRequest;
import com.reoagms.identity_service.user.dto.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(UUID id);

    List<UserResponse> getAllUsers();

    void deleteUser(UUID id);

}