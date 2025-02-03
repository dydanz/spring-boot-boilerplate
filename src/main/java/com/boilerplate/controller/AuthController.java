package com.boilerplate.controller;

import com.boilerplate.dto.AuthResponseDto;
import com.boilerplate.dto.LoginRequestDto;
import com.boilerplate.dto.UserRegistrationDto;
import com.boilerplate.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(
            @Valid @RequestBody UserRegistrationDto request
    ) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @Valid @RequestBody LoginRequestDto request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponseDto> verifyOtp(
            @RequestParam String email,
            @RequestParam String otp
    ) {
        return ResponseEntity.ok(authService.verifyOtp(email, otp));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponseDto> refreshToken(
            @RequestHeader("Authorization") String token
    ) {
        return ResponseEntity.ok(authService.refreshToken(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String token
    ) {
        authService.logout(token);
        return ResponseEntity.ok().build();
    }
} 