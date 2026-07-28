package com.reoagms.identity.auth.service;

import com.reoagms.identity.auth.dto.AuthResponse;
import com.reoagms.identity.auth.dto.LoginRequest;
import com.reoagms.identity.auth.dto.RegisterRequest;

public interface AuthenticationService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

}