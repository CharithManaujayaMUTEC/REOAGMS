package com.reoagms.api_gateway.jwt;

public interface JwtService {

    String extractUsername(String token);

    boolean isTokenValid(String token);

}