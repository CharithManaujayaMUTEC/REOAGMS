package com.reoagms.identity.user.service;

import com.reoagms.identity.user.dto.CreateUserRequest;
import com.reoagms.identity.user.dto.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(UUID id);

    List<UserResponse> getAllUsers();

    void deleteUser(UUID id);

}