package com.reoagms.identity_service.auth.service;

import com.reoagms.identity_service.auth.dto.AuthResponse;
import com.reoagms.identity_service.auth.dto.LoginRequest;
import com.reoagms.identity_service.auth.dto.RegisterRequest;

public interface AuthenticationService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

}