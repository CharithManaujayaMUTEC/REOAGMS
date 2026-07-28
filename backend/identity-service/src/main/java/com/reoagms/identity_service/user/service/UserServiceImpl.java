package com.reoagms.identity_service.user.service;

import com.reoagms.identity_service.common.enums.RoleType;
import com.reoagms.identity_service.exception.EmailAlreadyExistsException;
import com.reoagms.identity_service.exception.RoleNotFoundException;
import com.reoagms.identity_service.exception.UserNotFoundException;
import com.reoagms.identity_service.role.model.Role;
import com.reoagms.identity_service.role.repository.RoleRepository;
import com.reoagms.identity_service.user.dto.CreateUserRequest;
import com.reoagms.identity_service.user.dto.UserResponse;
import com.reoagms.identity_service.user.mapper.UserMapper;
import com.reoagms.identity_service.user.model.User;
import com.reoagms.identity_service.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(CreateUserRequest request) {

        if (repository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists.");
        }

        Role role = roleRepository.findByName(
                RoleType.valueOf(request.getRole())
        ).orElseThrow(() ->
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

        repository.save(user);

        return UserMapper.toResponse(user);

    }

    @Override
    public UserResponse getUserById(UUID id) {

        User user = repository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found."));

        return UserMapper.toResponse(user);

    }

    @Override
    public List<UserResponse> getAllUsers() {

        return repository.findAll()
                .stream()
                .map(UserMapper::toResponse)
                .toList();

    }

    @Override
    public void deleteUser(UUID id) {

        User user = repository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found."));

        repository.delete(user);

    }

}