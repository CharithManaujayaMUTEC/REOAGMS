package com.reoagms.api_gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handle(Exception ex) {

        return ResponseEntity

                .status(HttpStatus.UNAUTHORIZED)

                .body(

                        Map.of(

                                "timestamp", LocalDateTime.now(),

                                "status", 401,

                                "message", ex.getMessage()

                        )

                );

    }

}