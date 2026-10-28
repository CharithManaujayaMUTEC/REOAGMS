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
import org.springframework.security.core.AuthenticationException;
import com.reoagms.identity_service.audit.AuditService;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditService auditService;

    @Override
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists.");
        }

        // Self-registration is always the least-privileged role; admins assign other roles via /users.
        RoleType roleType = RoleType.PLANT_OPERATOR;

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
        auditService.record(user.getEmail(), "USER_REGISTERED", "User", "Self-registered as " + roleType);

        String token = jwtService.generateToken(Map.of("role", user.getRole().getName().name()), user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().getName().name())
                .build();

    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (AuthenticationException ex) {
            auditService.record(request.getEmail(), "LOGIN_FAILED", "User", ex.getClass().getSimpleName());
            throw ex;
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        String token = jwtService.generateToken(Map.of("role", user.getRole().getName().name()), user.getUsername());

        auditService.record(user.getEmail(), "LOGIN_SUCCESS", "User", "Role " + user.getRole().getName());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().getName().name())
                .build();

    }

}