package com.reoagms.api_gateway.filter;

import tools.jackson.databind.ObjectMapper;
import com.reoagms.api_gateway.jwt.JwtService;
import com.reoagms.api_gateway.util.RolePolicy;
import com.reoagms.api_gateway.util.RouteValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/** Enforces bearer-token validation for API requests at the Gateway servlet boundary. */
@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final RouteValidator validator;
    private final RolePolicy rolePolicy;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/") || "OPTIONS".equalsIgnoreCase(request.getMethod())
                || !validator.isSecured.test(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ") || !jwtService.isTokenValid(header.substring(7))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(), Map.of(
                    "status", 401, "error", "Unauthorized",
                    "message", "A valid Bearer token is required to access this API."));
            return;
        }
        String role = jwtService.extractRole(header.substring(7));
        if (!rolePolicy.isAllowed(request.getMethod(), request.getRequestURI(), role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(), Map.of(
                    "status", 403, "error", "Forbidden",
                    "message", "Your role (" + role + ") is not permitted to perform this action."));
            return;
        }
        chain.doFilter(request, response);
    }

    public boolean isAuthorized(String path, String authorizationHeader) {
        if (!validator.isSecured.test(path)) return true;
        return authorizationHeader != null && authorizationHeader.startsWith("Bearer ")
                && jwtService.isTokenValid(authorizationHeader.substring(7));
    }

    public String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) return null;
        return authorizationHeader.substring(7);
    }
}
