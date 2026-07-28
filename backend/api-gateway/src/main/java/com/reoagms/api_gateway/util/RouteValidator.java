package com.reoagms.api_gateway.util;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    public static final List<String> OPEN_API_ENDPOINTS = List.of(

            "/api/v1/auth/login",

            "/api/v1/auth/register"

    );

    public Predicate<String> isSecured =

            uri -> OPEN_API_ENDPOINTS

                    .stream()

                    .noneMatch(uri::contains);

}