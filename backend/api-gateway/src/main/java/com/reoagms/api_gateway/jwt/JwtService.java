package com.reoagms.api_gateway.jwt;

public interface JwtService {

    boolean isTokenValid(String token);

    String extractUsername(String token);

}