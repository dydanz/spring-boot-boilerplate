package com.server.app.controller;

import com.server.app.dto.AuthenticationRequest;
import com.server.app.dto.AuthenticationResponse;
import com.server.app.dto.RegisterRequest;
import com.server.app.dto.VerifyOtpRequest;
import com.server.app.dto.ResetOtpRequest;
import com.server.app.dto.LogoutRequest;
import com.server.app.service.AuthenticationService;
import io.github.bucket4j.Bucket;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final Bucket bucket;

    private ResponseEntity<AuthenticationResponse> returnTooManyRequest(String message) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(AuthenticationResponse.builder()
        .message(message)
        .success(false)
        .build());
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        if (!bucket.tryConsume(1)) {
            return returnTooManyRequest("Too many registration attempts. Please try again later.");
        }

        try {
            if(request.getEmail() == null || request.getEmail().isEmpty() 
            || request.getPassword() == null || request.getPassword().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AuthenticationResponse.builder()
                      .message("Email and Password is required")
                      .success(false)
                      .build());
            }
            return ResponseEntity.ok(authenticationService.register(request));
        } catch (Exception e) {
            log.error("Registration error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthenticationResponse.builder()
                            .success(false)
                            .message("Something went wrong on our end. We're working on it—please try again later.")
                            .build());
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthenticationResponse> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request
    ) {
        if (!bucket.tryConsume(1)) {
            return returnTooManyRequest("Too many OTP verification attempts. Please try again later.");
        }

        try {
            if(request.getEmail() == null || request.getEmail().isEmpty() || 
            request.getOtp() == null || request.getOtp().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AuthenticationResponse.builder()
                       .message("Email and OTP Code is required")
                       .success(false)
                       .build());
            }
            return ResponseEntity.ok(authenticationService.verifyOtp(request));
        } catch (Exception e) {
            log.error("OTP verification error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthenticationResponse.builder()
                            .success(false)
                            .message("Something went wrong on our end. We're working on it—please try again later.")
                            .build());
        }
    }

    @PostMapping("/reset-otp")
    public ResponseEntity<AuthenticationResponse> resetOtp(
            @Valid @RequestBody ResetOtpRequest request
    ) {
        if (!bucket.tryConsume(1)) {
            return returnTooManyRequest("Too many OTP Reset attempts. Please try again later");
        }

        return ResponseEntity.ok(authenticationService.resetOtp(request.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request,
            HttpServletRequest httpRequest
    ) {
        if (!bucket.tryConsume(1)) {
            return returnTooManyRequest("Too many Login attempts. Please try again later");
        }

        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        return ResponseEntity.ok(authenticationService.authenticate(request, ipAddress, userAgent));
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthenticationResponse> logout(
            @RequestBody LogoutRequest request,
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest httpRequest
    ) {
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        AuthenticationResponse response = authenticationService.logout(token, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }
}