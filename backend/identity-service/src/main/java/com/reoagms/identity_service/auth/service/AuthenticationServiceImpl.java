package com.reoagms.identity_service.auth.service;

import com.reoagms.identity_service.auth.dto.AuthResponse;
import com.reoagms.identity_service.auth.dto.LoginRequest;
import com.reoagms.identity_service.auth.dto.RegisterRequest;
import com.reoagms.identity_service.common.enums.RoleType;
import com.reoagms.identity_service.exception.EmailAlreadyExistsException;
import com.reoagms.identity_service.exception.RoleNotFoundException;
import com.reoagms.identity_service.role.model.Role;
import com.reoagms.identity_service.role.repository.RoleRepository;
import com.reoagms.identity_service.security.jwt.JwtService;
import com.reoagms.identity_service.user.model.User;
import com.reoagms.identity_service.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists.");
        }

        RoleType roleType = request.getRole() == null
                ? RoleType.PLANT_OPERATOR
                : request.getRole();

        Role role = roleRepository.findByName(roleType)
                .orElseThrow(() ->
                        new RoleNotFoundException("Role not found."));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .enabled(true)
                .accountNonLocked(true)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .build();

    }

    @Override
    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(

                new UsernamePasswordAuthenticationToken(

                        request.getEmail(),

                        request.getPassword()

                )

        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        String token = jwtService.generateToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .build();

    }

}