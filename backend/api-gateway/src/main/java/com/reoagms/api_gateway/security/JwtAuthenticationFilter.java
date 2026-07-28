package com.reoagms.api_gateway.security;

import com.reoagms.api_gateway.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter {

    private final JwtService jwtService;

    public boolean validate(String token) {

        return jwtService.isTokenValid(token);

    }

}