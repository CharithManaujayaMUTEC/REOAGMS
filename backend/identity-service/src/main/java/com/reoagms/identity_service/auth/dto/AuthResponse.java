package com.reoagms.identity_service.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String token;

    private String email;

    private String role;

    private String firstName;

    private String lastName;

}