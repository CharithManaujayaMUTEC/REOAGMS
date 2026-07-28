package com.reoagms.identity.auth.controller;

import com.reoagms.identity.auth.dto.AuthResponse;
import com.reoagms.identity.auth.dto.LoginRequest;
import com.reoagms.identity.auth.dto.RegisterRequest;
import com.reoagms.identity.auth.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return authenticationService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authenticationService.login(request);
    }

}