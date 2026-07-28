package com.reoagms.identity_service.auth.controller;

import com.reoagms.identity_service.auth.dto.AuthResponse;
import com.reoagms.identity_service.auth.dto.LoginRequest;
import com.reoagms.identity_service.auth.dto.RegisterRequest;
import com.reoagms.identity_service.auth.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public AuthResponse register(
            @Valid @RequestBody RegisterRequest request) {

        return service.register(request);

    }

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request) {

        return service.login(request);

    }

}