package com.reoagms.identity_service.user.service;

import com.reoagms.identity_service.user.dto.CreateUserRequest;
import com.reoagms.identity_service.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public UserResponse getUserById(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public List<UserResponse> getAllUsers() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void deleteUser(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}