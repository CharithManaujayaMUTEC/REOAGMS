package com.reoagms.identity_service.user.controller;

import com.reoagms.identity_service.user.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    @GetMapping("/me")
    public User currentUser(
            @AuthenticationPrincipal User user) {

        return user;

    }

}