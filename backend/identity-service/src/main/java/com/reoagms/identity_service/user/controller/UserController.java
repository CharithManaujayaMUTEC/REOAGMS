package com.reoagms.identity_service.user.controller;

import com.reoagms.identity_service.user.dto.CreateUserRequest;
import com.reoagms.identity_service.user.dto.UserResponse;
import com.reoagms.identity_service.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @PostMapping
    public UserResponse createUser(
            @Valid @RequestBody CreateUserRequest request) {

        return service.createUser(request);

    }

    @GetMapping
    public List<UserResponse> getAllUsers() {

        return service.getAllUsers();

    }

    @GetMapping("/{id}")
    public UserResponse getUserById(
            @PathVariable UUID id) {

        return service.getUserById(id);

    }

    @DeleteMapping("/{id}")
    public void deleteUser(
            @PathVariable UUID id) {

        service.deleteUser(id);

    }

}