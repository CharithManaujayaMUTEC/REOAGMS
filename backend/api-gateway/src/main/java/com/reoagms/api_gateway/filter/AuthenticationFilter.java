package com.reoagms.api_gateway.filter;

import com.reoagms.api_gateway.util.RouteValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import com.reoagms.api_gateway.jwt.JwtService;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter {

    private final JwtService jwtService;

    private final RouteValidator validator;

    public boolean isAuthorized(String path, String authorizationHeader) {

        if (!validator.isSecured.test(path)) {
            return true;
        }

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            return false;

        }

        String token = authorizationHeader.substring(7);

        return jwtService.isTokenValid(token);

    }

    public String extractToken(String authorizationHeader) {

        if (authorizationHeader == null) {

            return null;

        }

        if (!authorizationHeader.startsWith("Bearer ")) {

            return null;

        }

        return authorizationHeader.substring(7);

    }

}