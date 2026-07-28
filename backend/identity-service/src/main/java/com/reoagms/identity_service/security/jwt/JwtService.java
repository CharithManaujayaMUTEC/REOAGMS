package com.reoagms.identity.security.jwt;

import io.jsonwebtoken.Claims;

import java.util.Map;

public interface JwtService {

    String extractUsername(String token);

    <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver);

    String generateToken(String username);

    String generateToken(Map<String, Object> extraClaims, String username);

    boolean isTokenValid(String token, String username);

}