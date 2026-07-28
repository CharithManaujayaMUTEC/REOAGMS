package com.reoagms.identity.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.security.jwt")
public record JwtProperties(

        String secretKey,

        Long expiration

) {
}