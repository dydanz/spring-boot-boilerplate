package com.boilerplate.controller;

import com.boilerplate.dto.AuthResponseDto;
import com.boilerplate.dto.LoginRequestDto;
import com.boilerplate.dto.UserRegistrationDto;
import com.boilerplate.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponseDto> register(
            @Valid @RequestBody UserRegistrationDto request
    ) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<AuthResponseDto> login(
            @Valid @RequestBody LoginRequestDto request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP for account activation")
    public ResponseEntity<AuthResponseDto> verifyOtp(
            @RequestParam String email,
            @RequestParam String otp
    ) {
        return ResponseEntity.ok(authService.verifyOtp(email, otp));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh authentication token")
    public ResponseEntity<AuthResponseDto> refreshToken(
            @RequestHeader("Authorization") String token
    ) {
        return ResponseEntity.ok(authService.refreshToken(token));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and invalidate token")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String token
    ) {
        authService.logout(token);
        return ResponseEntity.ok().build();
    }
} 