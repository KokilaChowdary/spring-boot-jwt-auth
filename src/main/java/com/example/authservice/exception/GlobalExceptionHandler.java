package com.example.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 🔹 Validation errors (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldError()
                .getDefaultMessage();

        ApiError error = new ApiError(
                400,
                "Validation failed",
                message
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 🔹 Runtime errors (fallback)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntime(RuntimeException ex) {

        ApiError error = new ApiError(
                400,
                "Bad request",
                ex.getMessage()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 🔹 Unauthorized
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuth(Exception ex) {

        ApiError error = new ApiError(
                401,
                "Unauthorized",
                "Invalid username or password"
        );

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }
}
